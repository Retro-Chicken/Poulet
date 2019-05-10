package poulet.interpreter;

import poulet.ast.*;
import poulet.temp.TempQuoter;
import poulet.typing.Checker;
import poulet.typing.Context;
import poulet.value.*;

import java.io.PrintWriter;
import java.util.*;
import java.util.function.Function;

public class Interpreter {
    private Program program;

    public Interpreter(Program program) {
        this.program = program;
    }

    public static void run(Program program, PrintWriter out) throws Exception {
        program = transform(program);
        Context context = new Context();

        for (TopLevel topLevel : program.program) {
            if (topLevel instanceof Definition) {
                Definition definition = (Definition) topLevel;
                Checker.checkKind(definition.type, context);
                if (definition.definition != null)
                    Checker.checkType(definition.definition, evaluateExpression(definition.type), context);
                context = context.append(definition.name, evaluateExpression(definition.type));
            } else if (topLevel instanceof Print) {
                Print print = (Print) topLevel;
                switch (print.command) {
                    case reduce:
                        out.println(evaluateExpression(print.expression));
                        break;
                    case check:
                        out.println(Checker.deduceType(print.expression, context));//cleanCheck(Checker.deduceType(print.expression, context).expression(), 0));
                        break;
                }
            }
        }
    }
    /*
    private static Expression cleanCheck(Expression expression, int depth) {
        if(expression instanceof Application) {
            Application application = (Application) expression;
            Expression function = cleanCheck(application.function, depth);
            Expression argument = cleanCheck(application.argument, depth);
            return new Application(function, argument);
        } else if(expression instanceof Abstraction) {
            Abstraction abstraction = (Abstraction) expression;
            Expression type = cleanCheck(abstraction.type, depth);
            Expression body = cleanCheck(abstraction.body, depth + 1);
            return new Abstraction(null, type, body);
        } else if(expression instanceof Variable) {
            Variable variable = (Variable) expression;
            String name = variable.symbol.getName();
            if(name.matches("bound\\d+")) {
                int level = Integer.parseInt(name.substring(5));
                return new Variable(new Symbol(depth - level - 1));
            } else
                return variable;
        } else if(expression instanceof PiType) {
            PiType piType = (PiType) expression;
            Expression type = cleanCheck(piType.type, depth);
            Expression body = cleanCheck(piType.body, depth + 1);
            return new PiType(null, type, body);
        }
        return expression;
    }*/

    public static Program transform(Program program) throws DefinitionException {
        Program result = new Program(program);
        result = addIndices(result);
        result = substituteCalls(result);
        //result = annotate(result);
        return result;
    }

    public static Program annotate(Program program) {
        List<TopLevel> annotatedProgram = new ArrayList<>();

        for (TopLevel topLevel : program.program) {
            if (topLevel instanceof Definition) {
                Definition definition = (Definition) topLevel;
                Definition substituted = new Definition(
                        definition.name,
                        annotate(definition.type),
                        annotate(definition.definition)
                );
                annotatedProgram.add(substituted);
            } else if (topLevel instanceof Print) {
                Print print = (Print) topLevel;
                Print substituted = new Print(print.command, annotate(print.expression));
                annotatedProgram.add(substituted);
            } else {
                annotatedProgram.add(topLevel);
            }
        }

        return new Program(annotatedProgram);
    }
    public static Expression annotate(Expression expression) {
        if (expression instanceof Abstraction) {
            Abstraction abstraction = (Abstraction) expression;
            Abstraction newAbstraction = new Abstraction(abstraction.symbol, null, abstraction.body);
            return new Annotation(newAbstraction, abstraction.type);
        } else if (expression instanceof Application) {
            Application application = (Application) expression;
            return new Application(
                    annotate(application.function),
                    annotate(application.argument)
            );
        } else if (expression instanceof PiType){
            PiType piType = (PiType) expression;
            return new PiType(
                    piType.variable,
                    annotate(piType.type),
                    annotate(piType.body)
            );
        }
        return expression;
    }

    public static Program substituteCalls(Program program) throws DefinitionException {
        Set<Symbol> definedSymbols = new HashSet<>();

        for (TopLevel topLevel : program.program) {
            if (topLevel instanceof Definition) {
                Definition definition = (Definition) topLevel;
                if (definedSymbols.contains(definition.name)) {
                    throw new DefinitionException("symbol " + definition.name + " is defined twice");
                }
                definedSymbols.add(definition.name);
            }
        }

        List<TopLevel> substitutedProgram = new ArrayList<>();

        for (TopLevel topLevel : program.program) {
            if (topLevel instanceof Definition) {
                Definition definition = (Definition) topLevel;
                Definition substituted = new Definition(
                        definition.name,
                        substituteDefinitions(definition.type, new Program(substitutedProgram)),
                        substituteDefinitions(definition.definition, new Program(substitutedProgram))
                );
                substitutedProgram.add(substituted);
            } else if (topLevel instanceof Print) {
                Print print = (Print) topLevel;
                Print substituted = new Print(print.command, substituteDefinitions(print.expression, new Program(substitutedProgram)));
                substitutedProgram.add(substituted);
            } else { // TODO: handle type declaration
                substitutedProgram.add(topLevel);
            }
        }

        return new Program(substitutedProgram);
    }

    private static Expression substituteDefinitions(Expression expression, Program program) {
        Expression substituted = expression;
        List<Definition> definitions = getDefinitions(program);
        for (int i = definitions.size() - 1; i >= 0; i--) {
            Definition definition = definitions.get(i);
            if (definition.definition != null) {
                substituted = substitute(substituted, definition.name, definition.definition);//definition.definition.transform("T" + new Random().nextInt(10000)));
            }
        }
        return substituted;
    }

    /*private static Expression substituteExpression(Expression base, Symbol name, Expression substitute) {
        if(base instanceof Abstraction) {
            Abstraction abstraction = (Abstraction) base;
            return new Abstraction(abstraction.symbol, abstraction.type, substituteExpression(abstraction.body, name, substitute));
        } else if(base instanceof Application) {
            Application application = (Application) base;
            return new Application(substituteExpression(application.function, name, substitute), substituteExpression(application.argument, name, substitute));
        } else if(base instanceof Variable) {
            Variable variable = (Variable) base;
            if(variable.symbol.weakEquals(name))
                return substitute;
            else
                return base;
        }

        return null;
    }*/

    public static List<Definition> getDefinitions(Program program) {
        ArrayList<Definition> definitions = new ArrayList<>();
        for (TopLevel topLevel : program.program) {
            if (topLevel instanceof Definition) {
                Definition definition = (Definition) topLevel;
                definitions.add(definition);
            }
        }
        return definitions;
    }

    public static List<Print> getPrints(Program program) {
        ArrayList<Print> prints = new ArrayList<>();
        for (TopLevel topLevel : program.program) {
            if (topLevel instanceof Print) {
                Print print = (Print) topLevel;
                prints.add(print);
            }
        }
        return prints;
    }

    public static List<Expression> getExpressions(Program program) {
        ArrayList<Expression> expressions = new ArrayList<>();
        for (TopLevel topLevel : program.program) {
            if (topLevel instanceof Definition) {
                Definition definition = (Definition) topLevel;
                expressions.add(definition.definition);
            }
        }
        return expressions;
    }

    /*public static Value evaluateExpression(Expression expression) throws Exception {
        if (expression instanceof Variable) {

        } else if (expression instanceof Abstraction) {

        } else if (expression instanceof Application) {
            Application application = (Application) expression;
            Expression function = evaluateExpression(application.function);
            Expression argument = evaluateExpression(application.argument);

            if (function instanceof Abstraction && isValue(argument)) {
                Abstraction abstraction = (Abstraction) function;

                if (abstraction.symbol != null)
                    return evaluateExpression(substitute(abstraction.body, abstraction.symbol, argument));
                else
                    return evaluateExpression(substitute(abstraction.body, new Symbol(0), argument));
            } else {
                return new Application(function, argument);
            }
        } else if (expression instanceof PiType) {
            PiType piType = (PiType) expression;
            PiType result = new PiType(
                    piType.variable,
                    evaluateExpression(piType.type, )
            );
        }

        throw new Exception("can't eval " + expression);
    }*/

    public static Value evaluateExpression(Expression expression) {
        return evaluateExpression(expression, new ArrayList<>());
    }

    public static Value evaluateExpression(Expression expression, List<Value> bound) {
        return evaluateExpression(expression, bound, 0);
    }
    public static Value evaluateExpression(Expression expression, List<Value> bound, int depth) {
        if (expression instanceof Variable) {
            Variable variable = (Variable) expression;

            if (variable.symbol.isFree()) {
                String name = variable.symbol.getName();
                if (name.matches("Type\\d+")) {
                    int level = Integer.parseInt(name.substring(4));
                    return new VType(level);
                } else {
                    return new VNeutral(new NFree(variable.symbol));
                }
            } else {
                int index = variable.symbol.getIndex();
                return bound.get(index);
            }
        } else if (expression instanceof Application) {
            Application application = (Application) expression;
            Value function = evaluateExpression(application.function, bound, depth + 1);
            Value argument = evaluateExpression(application.argument, bound, depth + 1);

            if (function instanceof VAbstraction) {
                VAbstraction abstraction = (VAbstraction) function;
                return abstraction.call(argument);
            } else if (function instanceof VNeutral) {
                VNeutral neutral = (VNeutral) function;
                return new VNeutral(new NApplication(neutral.neutral, argument));
            } else {
                System.err.println("can't apply to a " + function.getClass().getSimpleName());
                return null;
            }
        } else if (expression instanceof Abstraction) {
            Abstraction abstraction = (Abstraction) expression;

            assert abstraction.symbol == null;

            return new VAbstraction(argument -> {
                List<Value> newBound = new ArrayList<>();
                newBound.add(argument);
                newBound.addAll(bound);
                return evaluateExpression(abstraction.body, newBound, depth + 1);
            });
        } else if (expression instanceof PiType) {
            PiType piType = (PiType) expression;
            Value type = evaluateExpression(piType.type, bound, depth + 1);
            Function<Value, Value> body = argument -> {
                List<Value> newBound = new ArrayList<>();
                newBound.add(argument);
                newBound.addAll(bound);
                return evaluateExpression(piType.body, newBound, depth + 1);
            };
            return new VPi(type, body);
        } else if(expression instanceof Annotation) {
            Annotation annotation = (Annotation) expression;
            return evaluateExpression(annotation.expression, bound, depth);
        }

        return null;
    }

    private static Expression substitute(Expression expression, Symbol symbol, Expression substitution) {
        Expression result = null;

        if (expression instanceof Variable) {
            Variable variable = (Variable) expression;
            if (variable.symbol.equals(symbol)) {
                result = substitution;
            } else {
                result = expression;
            }
        } else if (expression instanceof Application) {
            Application application = (Application) expression;
            Expression function = substitute(application.function, symbol, substitution);
            Expression argument = substitute(application.argument, symbol, substitution);
            result = new Application(function, argument);
        } else if (expression instanceof Abstraction) {
            Abstraction abstraction = (Abstraction) expression;
            if (abstraction.symbol != null && abstraction.symbol.equals(symbol)) {
                result = expression;
            } else {
                Expression type = substitute(abstraction.type, symbol, substitution);
                Expression body = substitute(abstraction.body, symbol.increment(), substitution);
                result = new Abstraction(abstraction.symbol, type, body);
            }
        } else if (expression instanceof PiType) {
            PiType piType = (PiType) expression;
            Expression type = substitute(piType.type, symbol, substitution);
            Expression body = substitute(piType.body, symbol.increment(), substitution);
            result = new PiType(piType.variable, type, body);
        }

        // enforce unique symbol IDs
        //if (result != null)
        //    addIndices(result);

        return result;
    }

    public static Program addIndices(Program program) {
        List<TopLevel> result = new ArrayList<>();
        for (TopLevel topLevel : program.program) {
            if (topLevel instanceof Definition) {
                Definition definition = (Definition) topLevel;
                Definition indexed = new Definition(
                        definition.name,
                        addIndices(definition.type),
                        addIndices(definition.definition)
                );
                result.add(indexed);
            } else if (topLevel instanceof Print) {
                Print print = (Print) topLevel;
                Print indexed = new Print(print.command, addIndices(print.expression));
                result.add(indexed);
            } else { // TODO: handle type declaration
                result.add(topLevel);
            }
        }
        return new Program(result);
    }

    public static Expression addIndices(Expression expression) {
        return addIndices(new Stack<>(), expression);
    }

    private static Expression addIndices(Stack<Symbol> stack, Expression expression) {
        if (expression instanceof Abstraction) {
            Abstraction abstraction = (Abstraction) expression;
            // need to number types later
            Stack<Symbol> newStack = new Stack<>();
            newStack.addAll(stack);
            newStack.push(abstraction.symbol);
            Expression type = addIndices(stack, abstraction.type);
            Expression body = addIndices(newStack, abstraction.body);
            return new Abstraction(null, type, body);
        } else if (expression instanceof Application) {
            Application application = (Application) expression;
            Expression function = addIndices(stack, application.function);
            Expression argument = addIndices(stack, application.argument);
            return new Application(function, argument);
        } else if (expression instanceof Variable) {
            Variable variable = (Variable) expression;
            int index = stack.search(variable.symbol); // 1-based

            if (index < 0) { // free
                return variable;
            } else { // bound
                Symbol symbol = new Symbol(index - 1);
                return new Variable(symbol);
            }
        } else if(expression instanceof PiType) {
            PiType piType = (PiType) expression;
            // need to number types later
            Stack<Symbol> newStack = new Stack<>();
            newStack.addAll(stack);
            newStack.push(piType.variable);
            Expression type = addIndices(stack, piType.type);
            Expression body = addIndices(newStack, piType.body);
            return new PiType(null, type, body);
        }

        return expression;
    }

    /*public static Expression addSymbolIDs(Expression expression) {
        if (expression instanceof Abstraction) {
            Abstraction abstraction = (Abstraction) expression;
            Symbol symbol = abstraction.symbol.bind();
            Expression body = tag(abstraction.body, symbol);
            body = addSymbolIDs(body);
            return new Abstraction(symbol, abstraction.type, body);
        } else if (expression instanceof Application) {
            Application application = (Application) expression;
            Expression function = addSymbolIDs(application.function);
            Expression argument = addSymbolIDs(application.argument);
            return new Application(function, argument);
        }

        return expression;
    }*/

    /*private static Expression tag(Expression expression, Symbol bound) {
        if (expression instanceof Variable) {
            Variable variable = (Variable) expression;

            if (variable.symbol.name.equals(bound.name)) {
                return new Variable(variable.symbol.copyID(bound));
            }

            return variable;
        } else if (expression instanceof Abstraction) {
            Abstraction abstraction = (Abstraction) expression;
            Expression body = tag(abstraction.body, bound);
            return new Abstraction(abstraction.symbol, abstraction.type, body);
        } else if (expression instanceof Application) {
            Application application = (Application) expression;
            Expression function = tag(application.function, bound);
            Expression argument = tag(application.argument, bound);
            return new Application(function, argument);
        }

        return null;
    }*/

    private static Set<Name> getFreeVariables(Expression expression) {
        if (expression instanceof Variable) {
            Variable variable = (Variable) expression;
            return new HashSet<Name>(Arrays.asList(variable.symbol));
        } else if (expression instanceof Abstraction) {
            Abstraction abstraction = (Abstraction) expression;
            Set<Name> bodyFree = getFreeVariables(abstraction.body);
            bodyFree.remove(abstraction.symbol);
            return bodyFree;
        } else if (expression instanceof Application) {
            Application application = (Application) expression;
            Set<Name> functionFree = getFreeVariables(application.function);
            Set<Name> argumentFree = getFreeVariables(application.argument);
            functionFree.addAll(argumentFree);
            return functionFree;
        }

        return null;
    }

    private static boolean isValue(Expression expression) {
        return expression instanceof Abstraction || expression instanceof Variable;
    }
}
