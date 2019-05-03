package poulet.interpreter;

import poulet.ast.*;

import java.util.*;

public class Interpreter {
    private Program program;

    public Interpreter(Program program) {
        this.program = program;
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

        for(TopLevel topLevel : program.program) {
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
            substituted = substitute(substituted, definition.name, definition.definition);
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

    public static Expression evaluateExpression(Expression expression) {
        if (isValue(expression))
            return expression;

        if (expression instanceof Application) {
            Application application = (Application) expression;
            Expression function = evaluateExpression(application.function);
            Expression argument = evaluateExpression(application.argument);

            if (function instanceof Abstraction && isValue(argument)) {
                Abstraction abstraction = (Abstraction) function;
                return evaluateExpression(substitute(abstraction.body, abstraction.symbol, argument));
            } else {
                return new Application(function, argument);
            }

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
            if (abstraction.symbol.equals(symbol)) {
                result = expression;
            } else {
                Expression body = substitute(abstraction.body, symbol, substitution);
                result = new Abstraction(abstraction.symbol, abstraction.type, body);
            }
        }

        // enforce unique symbol IDs
        if (result != null)
            addIndices(result);

        return result;
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
            Expression body = addIndices(newStack, abstraction.body);
            return new Abstraction(null, abstraction.type, body);
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

    private static Set<Symbol> getFreeVariables(Expression expression) {
        if (expression instanceof Variable) {
            Variable variable = (Variable) expression;
            return new HashSet<Symbol>(Arrays.asList(variable.symbol));
        } else if (expression instanceof Abstraction) {
            Abstraction abstraction = (Abstraction) expression;
            Set<Symbol> bodyFree = getFreeVariables(abstraction.body);
            bodyFree.remove(abstraction.symbol);
            return bodyFree;
        } else if (expression instanceof Application) {
            Application application = (Application) expression;
            Set<Symbol> functionFree = getFreeVariables(application.function);
            Set<Symbol> argumentFree = getFreeVariables(application.argument);
            functionFree.addAll(argumentFree);
            return functionFree;
        }

        return null;
    }

    private static boolean isValue(Expression expression) {
        return expression instanceof Abstraction || expression instanceof Variable;
    }
}
