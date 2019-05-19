package poulet.interpreter;

import poulet.ast.*;
import poulet.quote.Quoter;
import poulet.typing.Checker;
import poulet.typing.Environment;
import poulet.value.*;

import java.io.PrintWriter;
import java.util.*;
import java.util.function.Function;

public class Interpreter {
    public static void run(Program program, PrintWriter out) throws Exception {
        // program = transform(program);
        program = makeSymbolsUnique(program);
        System.out.println("unique = " + program);
        Environment environment = new Environment();

        for (TopLevel topLevel : program.program) {
            if (topLevel instanceof Definition) {
                Definition definition = (Definition) topLevel;
                if (definition.definition != null)
                    environment = environment.appendScope(definition.name, definition.definition);
            } else if (topLevel instanceof InductiveDeclaration) {
                InductiveDeclaration inductiveDeclaration = (InductiveDeclaration) topLevel;
                environment = environment.appendInductive(inductiveDeclaration);
            }
        }
        for (TopLevel topLevel : program.program) {
            if (topLevel instanceof Definition) {
                Definition definition = (Definition) topLevel;
                if (definition.definition != null)
                    Checker.checkType(definition.definition, definition.type, environment);
                environment = environment.appendType(definition.name, definition.type);
            }
        }
        for (TopLevel topLevel : program.program) {
            try {
                if (topLevel instanceof Print) {
                    Print print = (Print) topLevel;
                    switch (print.command) {
                        case reduce:
                            out.println(evaluateExpression(print.expression, environment));
                            break;
                        case check:
                            out.println(Checker.deduceType(print.expression, environment)); //cleanCheck(Checker.deduceType(print.expression, context).expression(), 0));
                            break;
                    }
                } else if (topLevel instanceof Output) {
                    Output ouput = (Output) topLevel;
                    out.println(ouput.text);
                }
            } catch (Exception e) {
                System.err.println("Error on Line: " + topLevel);
                e.printStackTrace();
                throw new Exception();
            }
        }
    }

    public static Program transform(Program program) throws DefinitionException {
        Program result = new Program(program);
        result = makeSymbolsUnique(result);
        result = substituteCalls(result);
        //result = annotate(result);
        return result;
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
                Expression substitutedType = substituteDefinitions(definition.type, new Program(substitutedProgram));
                Expression substitutedDefinition = substituteDefinitions(definition.definition, new Program(substitutedProgram));
                Definition substituted = new Definition(
                        definition.name,
                        substitutedType,
                        substitutedDefinition
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

    public static Value evaluateExpression(Expression expression, Environment environment) {
        if (expression instanceof Variable) {
            Variable variable = (Variable) expression;

            if (variable.isFree() && variable.symbol.name.matches("Type\\d+")) {
                int level = Integer.parseInt(variable.symbol.name.substring(4));
                return new VType(level);
            } else {
                Expression definition = environment.lookUpScope(variable.symbol);

                if (definition == null) {
                    return new VNeutral(new NFree(variable.symbol));
                } else {
                    return evaluateExpression(definition, environment);
                }
            }
        } else if (expression instanceof Application) {
            Application application = (Application) expression;
            Value function = evaluateExpression(application.function, environment);
            Value argument = evaluateExpression(application.argument, environment);

            if (function instanceof VAbstraction) {
                VAbstraction abstraction = (VAbstraction) function;
                return abstraction.call(argument);
            } else if (function instanceof VNeutral) {
                VNeutral neutral = (VNeutral) function;
                return new VNeutral(new NApplication(neutral.neutral, argument));
            } else if (function instanceof VPi) { // TODO: Check if this is even sound
                VPi vPi = (VPi) function;
                return vPi.call(argument);
            } else if (function instanceof VFix) {
                // TODO: how the fuck does this work lmao
                VFix fix = (VFix) function;
                Definition callingDefinition = null;
                Environment newEnvironment = environment;

                for (Definition definition : fix.definitions) {
                    if (definition.name.equals(fix.symbol)) {
                        callingDefinition = definition;
                    }
                    Fix newFix = new Fix(fix.definitions, definition.name);
                    newEnvironment = newEnvironment.appendScope(definition.name, newFix);
                }

                if (callingDefinition == null) {
                    System.err.println("function " + fix.symbol + " not defined in " + fix);
                    return null;
                }

                return evaluateExpression(callingDefinition.definition, newEnvironment);
            } else {
                System.err.println("can't apply to a " + function.getClass().getSimpleName());
                return null;
            }
        } else if (expression instanceof Abstraction) {
            Abstraction abstraction = (Abstraction) expression;

            return new VAbstraction(evaluateExpression(abstraction.type, environment), argument -> {
                Environment newEnvironment = environment.appendScope(abstraction.symbol, argument.expression());
                return evaluateExpression(abstraction.body, newEnvironment);
            });
        } else if (expression instanceof PiType) {
            PiType piType = (PiType) expression;
            Value type = evaluateExpression(piType.type, environment);
            Function<Value, Value> body = argument -> {
                Environment newEnvironment = environment.appendScope(piType.variable, argument.expression());
                //System.out.println("pb = " + piType.body + ", nb = " + newBound);
                return evaluateExpression(piType.body, newEnvironment);
            };
            return new VPi(type, body);
        } else if (expression instanceof InductiveType) {
            InductiveType inductiveType = (InductiveType) expression;
            TypeDeclaration td = environment.lookUpTypeDeclaration(inductiveType.type);

            if (td == null) {
                System.err.println("inductive type " + inductiveType.type + " doesn't exist");
                return null;
            }

            List<Value> parameters = new ArrayList<>();

            if (inductiveType.parameters.size() != td.parameters.size()) {
                System.err.println("wrong number of parameters");
                return null;
            }

            for (Expression parameter : inductiveType.parameters) {
                parameters.add(evaluateExpression(parameter, environment));
            }

            if (inductiveType.isConcrete()) {
                List<Value> arguments = new ArrayList<>();

                // do we need to change environment here?
                // TODO think
                for (Expression argument : inductiveType.arguments)
                    arguments.add(evaluateExpression(argument, environment));

                return new VInductiveType(td, parameters, arguments);
            } else {
                Value type = evaluateExpression(td.type, environment);
                return inductiveTypeToValue(type, td, parameters);
            }
        } else if (expression instanceof ConstructorCall) {
            ConstructorCall constructorCall = (ConstructorCall) expression;
            Constructor constructor = environment.lookUpConstructor(constructorCall);

            if (constructor == null) {
                System.err.println("constructor " + constructor.name + " doesn't exist");
                return null;
            }

            List<Value> parameters = new ArrayList<>();
            TypeDeclaration td = environment.lookUpTypeDeclaration(constructorCall.inductiveType.type);

            if (td == null) {
                System.err.println("inductive type " + constructorCall.inductiveType.type + " doesn't exist");
                return null;
            }

            Environment newEnvironment = environment;

            if (constructorCall.inductiveType.parameters.size() != td.parameters.size()) {
                System.err.println("wrong number of parameters");
                return null;
            }

            for (int i = 0; i < td.parameters.size(); i++) {
                Parameter parameter = td.parameters.get(i);
                Expression parameterExpression = constructorCall.inductiveType.parameters.get(i);
                newEnvironment = newEnvironment.appendScope(parameter.symbol, parameterExpression);
                parameters.add(evaluateExpression(parameterExpression, environment));
            }

            if (constructorCall.isConcrete()) {
                List<Value> arguments = new ArrayList<>();

                // do we need to change environment here?
                // TODO think
                for (Expression argument : constructorCall.arguments)
                    arguments.add(evaluateExpression(argument, environment));

                VInductiveType inductiveType = (VInductiveType) evaluateExpression(constructorCall.inductiveType, environment);
                return new VConstructed(inductiveType, parameters, constructor, arguments);
            } else {
                Value type = evaluateExpression(constructor.definition, newEnvironment);
                return constructorToValue(type, constructor, parameters);
            }
        } else if (expression instanceof Match) {
            Match match = (Match) expression;
            Value value = evaluateExpression(match.expression, environment);

            if (value instanceof VConstructed) {
                VConstructed constructed = (VConstructed) value;

                for (Match.Clause clause : match.clauses) {
                    if (clause.constructorSymbol.equals(constructed.constructor.name)) {
                        if (clause.argumentSymbols.size() != constructed.arguments.size()) {
                            System.err.println("wrong number of arguments for clause " + clause);
                            return null;
                        }

                        Environment newEnvironment = environment;

                        for (int i = 0; i < clause.argumentSymbols.size(); i++) {
                            Symbol argumentSymbol = clause.argumentSymbols.get(i);
                            // skeeeeetch
                            Expression argument = Quoter.quote(constructed.arguments.get(i));
                            newEnvironment = newEnvironment.appendScope(argumentSymbol, argument);
                        }

                        return evaluateExpression(clause.expression, newEnvironment);
                    }
                }

                System.err.println("no clause found for constructor " + constructed.constructor.name);
                return null;
            } else {
                System.out.println("v = " + value);
                System.err.println("can only match on constructed value");
                return null;
            }
        } else if (expression instanceof Fix) {
            Fix fix = (Fix) expression;
            return new VFix(fix.definitions, fix.symbol);
        }

        return null;
    }

    private static Value inductiveTypeToValue(Value type, TypeDeclaration typeDeclaration, List<Value> parameters) {
        return inductiveTypeToValue(type, typeDeclaration, parameters, new ArrayList<>());
    }

    private static Value inductiveTypeToValue(Value type, TypeDeclaration typeDeclaration, List<Value> parameters, List<Value> arguments) {
        if (type instanceof VPi) {
            VPi vPi = (VPi) type;
            return new VAbstraction(vPi.type, argument -> {
                List<Value> newArgs = new ArrayList<>(arguments);
                newArgs.add(argument);
                return inductiveTypeToValue(vPi.call(argument), typeDeclaration, parameters, newArgs);
            });
        } else if (type instanceof VType) {
            return new VInductiveType(typeDeclaration, parameters, arguments);
        } else {
            System.err.println("type declaration " + typeDeclaration + " invalid");
            return null;
        }
    }

    private static Value constructorToValue(Value type, Constructor constructor, List<Value> parameters) {
        return constructorToValue(type, constructor, parameters, new ArrayList<>());
    }

    private static Value constructorToValue(Value type, Constructor constructor, List<Value> parameters, List<Value> arguments) {
        if (type instanceof VPi) {
            VPi vPi = (VPi) type;
            return new VAbstraction(vPi.type, argument -> {
                List<Value> newArgs = new ArrayList<>(arguments);
                newArgs.add(argument);
                return constructorToValue(vPi.call(argument), constructor, parameters, newArgs);
            });
        } else if (type instanceof VInductiveType) {
            VInductiveType inductiveType = (VInductiveType) type;
            return new VConstructed(inductiveType, parameters, constructor, arguments);
        } else {
            System.err.println("constructor call to " + constructor.name + " invalid");
            return null;
        }
    }

    public static Expression substitute(Expression expression, Symbol symbol, Expression substitution) {
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
                Expression body = substitute(abstraction.body, symbol, substitution);
                result = new Abstraction(abstraction.symbol, type, body);
            }
        } else if (expression instanceof PiType) {
            PiType piType = (PiType) expression;
            Expression type = substitute(piType.type, symbol, substitution);
            Expression body = substitute(piType.body, symbol, substitution);
            assert piType.variable != null;
            result = new PiType(piType.variable, type, body);
        } else if (expression instanceof Match) {
            Match match = (Match) expression;
            Expression matchExpression = substitute(match.expression, symbol, substitution);
            Expression type = substitute(match.type, symbol, substitution);
            List<Match.Clause> clauses = new ArrayList<>();

            for (Match.Clause clause : match.clauses) {
                Expression clauseExpression = substitute(clause.expression, symbol, expression);
                Match.Clause newClause = new Match.Clause(
                        clause.constructorSymbol,
                        clause.argumentSymbols,
                        clauseExpression
                );
                clauses.add(newClause);
            }

            result = new Match(
                    matchExpression,
                    match.expressionSymbol,
                    match.argumentSymbols,
                    type,
                    clauses
            );
        } else if (expression instanceof Fix) {

        }

        return result;
    }

    public static Program makeSymbolsUnique(Program program) {
        List<TopLevel> result = new ArrayList<>();
        for (TopLevel topLevel : program.program) {
            if (topLevel instanceof Definition) {
                Definition definition = (Definition) topLevel;
                Definition indexed = new Definition(
                        definition.name,
                        makeSymbolsUnique(definition.type),
                        makeSymbolsUnique(definition.definition)
                );
                result.add(indexed);
            } else if (topLevel instanceof Print) {
                Print print = (Print) topLevel;
                Print unique = new Print(print.command, makeSymbolsUnique(print.expression));
                result.add(unique);
            } else if (topLevel instanceof InductiveDeclaration) {
                InductiveDeclaration inductiveDeclaration = (InductiveDeclaration) topLevel;
                List<TypeDeclaration> typeDeclarations = new ArrayList<>();

                for (TypeDeclaration typeDeclaration : inductiveDeclaration.typeDeclarations) {
                    typeDeclarations.add(makeSymbolsUnique(typeDeclaration));
                }

                InductiveDeclaration unique = new InductiveDeclaration(typeDeclarations);
                result.add(unique);
            }
        }

        return new Program(result);
    }

    public static TypeDeclaration makeSymbolsUnique(TypeDeclaration typeDeclaration) {
        List<Constructor> constructors = new ArrayList<>();

        for (Constructor constructor : typeDeclaration.constructors) {
            Constructor unique = new Constructor(constructor.name, makeSymbolsUnique(constructor.definition));
            constructors.add(unique);
        }

        return new TypeDeclaration(
                typeDeclaration.name,
                typeDeclaration.parameters,
                makeSymbolsUnique(typeDeclaration.type),
                constructors
        );
    }

    public static Expression makeSymbolsUnique(Expression expression) {
        return makeSymbolsUnique(new HashMap<>(), expression);
    }

    private static Expression makeSymbolsUnique(Map<Symbol, Symbol> map, Expression expression) {
        if (expression instanceof Abstraction) {
            Abstraction abstraction = (Abstraction) expression;
            // need to number types later
            Map<Symbol, Symbol> newMap = new HashMap<>(map);
            Symbol unique = abstraction.symbol.makeUnique();
            newMap.put(abstraction.symbol, unique);
            Expression type = makeSymbolsUnique(map, abstraction.type);
            Expression body = makeSymbolsUnique(newMap, abstraction.body);
            return new Abstraction(unique, type, body);
        } else if (expression instanceof Application) {
            Application application = (Application) expression;
            Expression function = makeSymbolsUnique(map, application.function);
            Expression argument = makeSymbolsUnique(map, application.argument);
            return new Application(function, argument);
        } else if (expression instanceof Variable) {
            Variable variable = (Variable) expression;
            Symbol symbol = map.get(variable.symbol); // 1-based

            if (symbol == null) { // free
                return variable;
            } else { // bound
                return new Variable(symbol.copy());
            }
        } else if (expression instanceof PiType) {
            PiType piType = (PiType) expression;
            // need to number types later
            Map<Symbol, Symbol> newMap = new HashMap<>(map);
            Symbol unique = piType.variable.makeUnique();
            map.put(piType.variable, unique);
            Expression type = makeSymbolsUnique(map, piType.type);
            Expression body = makeSymbolsUnique(newMap, piType.body);
            assert unique != null;
            return new PiType(unique, type, body);
        } else if (expression instanceof Match) {
            Match match = (Match) expression;
            Expression matchExpression = makeSymbolsUnique(map, match.expression);
            Expression type = makeSymbolsUnique(map, match.type);
            List<Match.Clause> clauses = new ArrayList<>();

            for (Match.Clause clause : match.clauses) {
                Match.Clause newClause = new Match.Clause(
                        clause.constructorSymbol,
                        clause.argumentSymbols,
                        makeSymbolsUnique(map, clause.expression)
                );
                clauses.add(newClause);
            }

            return new Match(
                    matchExpression,
                    match.expressionSymbol,
                    match.argumentSymbols,
                    type,
                    clauses
            );
        } else if (expression instanceof Fix) {
            Fix fix = (Fix) expression;
            List<Definition> definitions = new ArrayList<>();

            for (Definition definition : fix.definitions) {
                Definition newDefinition = new Definition(
                        definition.name,
                        makeSymbolsUnique(map, definition.type),
                        makeSymbolsUnique(map, definition.definition)
                );
                definitions.add(newDefinition);
            }

            return new Fix(definitions, fix.symbol);
        }

        return expression;
    }
}
