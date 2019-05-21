package poulet.typing;

import poulet.Util;
import poulet.ast.*;
import poulet.interpreter.Interpreter;
import poulet.value.*;

import java.util.*;
import java.util.stream.Collectors;

public class Checker {
    public static void checkType(Expression term, Expression type, Environment environment) throws TypeException {
        Expression deduced = deduceType(term, environment);
        // TODO: See if this is sound, check if they're checkEquivalent by beta reduction
        deduced = Interpreter.evaluateExpression(deduced, environment).expression();
        type = Interpreter.evaluateExpression(type, environment).expression();

        // Subtyping check first
        // TODO: Make sure this is allowed
        if (deduced instanceof Variable && type instanceof Variable) {
            String nameDeduced = ((Variable) deduced).symbol.name;
            String nameType = ((Variable) type).symbol.name;
            if (nameDeduced.matches("Type\\d+") && nameType.matches("Type\\d+")) {
                int level1 = Integer.parseInt(nameDeduced.substring(4));
                int level2 = Integer.parseInt(nameType.substring(4));
                if (level1 < level2)
                    return;
            }
        }

        checkEquivalent(term, deduced, type, environment);
    }

    public static boolean isEquivalent(Expression actual, Expression expected, Environment environment) throws TypeException {
        // skeeeeeetchy
        int oldNextId = Symbol.nextId;
        Symbol.nextId = 0;
        Expression newActual = Interpreter.makeSymbolsUnique(actual);
        Symbol.nextId = 0;
        Expression newExpected = Interpreter.makeSymbolsUnique(expected);
        Symbol.nextId = oldNextId;
        return newActual.toString().equals(newExpected.toString());
    }

    private static void checkEquivalent(Expression term, Expression actual, Expression expected, Environment environment) throws TypeException {
        // skeeeeeetchy
        int oldNextId = Symbol.nextId;
        Symbol.nextId = 0;
        Expression newActual = Interpreter.makeSymbolsUnique(actual);
        Symbol.nextId = 0;
        Expression newExpected = Interpreter.makeSymbolsUnique(expected);
        Symbol.nextId = oldNextId;

        if (!newActual.toString().equals(newExpected.toString()))
            throw new TypeException("Type Mismatch:\n" + term + " is of type " + newActual + ", not " + newExpected + " in env " + environment);
    }

    public static Expression deduceType(Expression term, Environment environment) throws TypeException {
        Expression result = null;

        if (term instanceof Abstraction) {
            Abstraction abstraction = (Abstraction) term;
            Expression abstractionType = Interpreter.evaluateExpression(abstraction.type, environment).expression();
            Environment newEnvironment = environment.appendType(abstraction.symbol, abstractionType);
            Expression bodyType = deduceType(abstraction.body, newEnvironment);
            result = new PiType(abstraction.symbol, abstractionType, bodyType);
        } else if (term instanceof Variable) {
            Variable variable = (Variable) term;
            Expression variableType = environment.lookUpType(variable.symbol);
            Expression definition = environment.lookUpScope(variable.symbol);

            if (variableType != null) {
                result = variableType;
            } else if (definition != null) {
                result = deduceType(definition, environment);
            } else {
                throw new TypeException("Unknown Identifier " + variable);
            }
        } else if (term instanceof Application) {
            Application application = (Application) term;

            /*if (applicationDecomposition.function instanceof ConstructorCall) {
                ConstructorCall constructorCall = (ConstructorCall) applicationDecomposition.function;
                deduceType(constructorCall, environment);

                Constructor constructor = environment.lookUpConstructor(constructorCall);
                TypeDeclaration typeDeclaration = environment.lookUpTypeDeclaration(constructorCall.inductiveType.type);
                Environment newEnvironment = environment;

                for (int i = 0; i < constructorCall.inductiveType.parameters.size(); i++) {
                    Expression parameterType = typeDeclaration.parameters.get(i).type;
                    Expression parameter = constructorCall.inductiveType.parameters.get(i);
                    checkType(parameter, parameterType, environment);
                    newEnvironment = newEnvironment.appendScope(typeDeclaration.parameters.get(i).symbol, parameter);
                }

                PiTypeDecomposition piTypeDecomposition = getPiTypeDecomposition(constructor.definition);
                System.out.println(">>>");
                System.out.println(constructor.definition);
                System.out.println(piTypeDecomposition.argumentTypes);
                System.out.println(piTypeDecomposition.bodyType);
                System.out.println(">>>");

                if (piTypeDecomposition.argumentTypes.size() != applicationDecomposition.arguments.size()) {
                    System.out.println(piTypeDecomposition.arguments);
                    System.out.println(applicationDecomposition.arguments);
                    throw new TypeException("wrong number of arguments");
                }

                for (int i = 0; i < piTypeDecomposition.argumentTypes.size(); i++) {
                    Expression argumentType = piTypeDecomposition.argumentTypes.get(i);
                    Expression argument = applicationDecomposition.arguments.get(i);
                    checkType(argument, argumentType, newEnvironment);
                    Symbol symbol = piTypeDecomposition.arguments.get(i);
                    newEnvironment = newEnvironment.appendScope(symbol, argument);
                }

                // TODO: fix
                // result = constructorCall.inductiveType;
                result = Interpreter.evaluateExpression(piTypeDecomposition.bodyType, newEnvironment).expression();
            } else {*/
            Expression functionType = deduceType(application.function, environment);

            if (functionType instanceof PiType) {
                PiType piType = (PiType) functionType;
                // TODO: Fix this
                checkType(application.argument, piType.type, environment);
                result = Interpreter.substitute(piType.body, piType.variable, application.argument);
                if (result == null)
                    System.out.printf("sub(%s, %s, %s)\n", piType.body, piType.variable, application.argument);
            } else if (functionType instanceof Variable) { // TODO: Double check this sketchy-ness
                Variable variable = (Variable) functionType;
                String name = variable.symbol.name;

                if (name.matches("Type\\d+")) {
                    if (application.function instanceof PiType) {
                        PiType piType = (PiType) application.function;
                        checkType(application.argument, piType.type, environment);
                        result = new Variable(new Symbol(name));
                    } else if (application.function instanceof Variable) {
                        Variable free = (Variable) application.function;
                        Expression definition = environment.lookUpScope(free.symbol);

                        if (definition instanceof PiType) {
                            PiType piType = (PiType) definition;
                            checkType(application.argument, piType.type, environment);
                            result = new Variable(new Symbol(name));
                        }
                    }
                }
            } else {
                throw new TypeException("can't apply to " + application.function);
            }
        } else if (term instanceof PiType) {
            PiType piType = (PiType) term;
            Expression typeLevel = deduceType(piType.type, environment);
            Symbol tempSymbol = new Symbol("temp").makeUnique();
            Environment newEnvironment = environment.appendType(tempSymbol, Interpreter.evaluateExpression(piType.type, environment).expression());
            Expression bodyLevel = deduceType(Interpreter.substitute(piType.body, piType.variable, new Variable(tempSymbol)), newEnvironment);
            if (typeLevel instanceof Variable && bodyLevel instanceof Variable) {
                Variable typeVar = (Variable) typeLevel;
                Variable bodyVar = (Variable) bodyLevel;
                String typeName = typeVar.symbol.name;
                String bodyName = bodyVar.symbol.name;
                if (typeName.matches("Type\\d+") && bodyName.matches("Type\\d+")) {
                    int level1 = Integer.parseInt(typeName.substring(4));
                    int level2 = Integer.parseInt(bodyName.substring(4));
                    result = new Variable(new Symbol("Type" + umax(level1, level2)));
                }
            }
        } else if (term instanceof InductiveType) {
            InductiveType inductiveType = (InductiveType) term;
            TypeDeclaration typeDeclaration = environment.lookUpTypeDeclaration(inductiveType.type);

            if (typeDeclaration == null)
                throw new TypeException("type declaration not found");

            if (inductiveType.parameters.size() != typeDeclaration.parameters.size())
                throw new TypeException("wrong number of parameters");

            Environment newEnvironment = environment;

            for (int i = 0; i < inductiveType.parameters.size(); i++) {
                Expression parameterType = typeDeclaration.parameters.get(i).type;
                Expression parameter = inductiveType.parameters.get(i);
                checkType(parameter, parameterType, environment);
                newEnvironment = newEnvironment.appendScope(typeDeclaration.parameters.get(i).symbol, parameter);
            }

            result = Interpreter.evaluateExpression(typeDeclaration.type, newEnvironment).expression();
        } else if (term instanceof ConstructorCall) {
            ConstructorCall constructorCall = (ConstructorCall) term;
            Constructor constructor = environment.lookUpConstructor(constructorCall);

            if (constructor == null)
                throw new TypeException("constructor not found");

            TypeDeclaration typeDeclaration = environment.lookUpTypeDeclaration(constructorCall.inductiveType.type);

            if (typeDeclaration == null)
                throw new TypeException("type declaration not found");

            if (constructorCall.inductiveType.parameters.size() != typeDeclaration.parameters.size())
                throw new TypeException("wrong number of parameters");

            Environment newEnvironment = environment;

            for (int i = 0; i < constructorCall.inductiveType.parameters.size(); i++) {
                Expression parameterType = typeDeclaration.parameters.get(i).type;
                Expression parameter = constructorCall.inductiveType.parameters.get(i);
                checkType(parameter, parameterType, environment);
                newEnvironment = newEnvironment.appendScope(typeDeclaration.parameters.get(i).symbol, parameter);
            }

            /*if (constructorCall.isConcrete()) {
                // TODO: using this environment is sketchy, but eh
                PiTypeDecomposition piTypeDecomposition = getPiTypeDecomposition(constructor.definition);

                if (piTypeDecomposition.argumentTypes.size() != constructorCall.arguments.size())
                    throw new TypeException("wrong number of arguments");

                for (int i = 0; i < piTypeDecomposition.argumentTypes.size(); i++) {
                    Expression argumentType = piTypeDecomposition.argumentTypes.get(i);
                    Expression argument = constructorCall.arguments.get(i);
                    checkType(argument, argumentType, newEnvironment);
                    Symbol symbol = piTypeDecomposition.arguments.get(i);
                    newEnvironment = newEnvironment.appendScope(symbol, argument);
                }

                // TODO: fix
                // result = constructorCall.inductiveType;
                result = Interpreter.evaluateExpression(piTypeDecomposition.bodyType, newEnvironment).expression();
            } else {*/
            result = Interpreter.evaluateExpression(constructor.definition, newEnvironment).expression();
            //}
        } else if (term instanceof Match) {
            Match match = (Match) term;
            Expression expressionType = deduceType(match.expression, environment);

            if (expressionType instanceof InductiveType) {
                InductiveType inductiveType = (InductiveType) expressionType;
                TypeDeclaration typeDeclaration = environment.lookUpTypeDeclaration(inductiveType.type);

                if (typeDeclaration == null)
                    throw new TypeException("type declaration " + inductiveType.type + " not found");

                PiTypeDecomposition piTypeDecomposition = getPiTypeDecomposition(typeDeclaration.type);
                if (match.argumentSymbols.size() != piTypeDecomposition.argumentTypes.size())
                    throw new TypeException("wrong number of arguments");

                Environment newEnvironment = environment.appendScope(match.expressionSymbol, match.expression);

                for (int i = 0; i < piTypeDecomposition.argumentTypes.size(); i++) {
                    Symbol symbol = match.argumentSymbols.get(i);
                    Expression argument = piTypeDecomposition.argumentTypes.get(i);
                    newEnvironment = newEnvironment.appendType(symbol, argument);
                    // TODO: need to add to scope too?
                }

                Expression returnType = Interpreter.evaluateExpression(match.type, newEnvironment).expression();

                for (Constructor constructor : typeDeclaration.constructors) {
                    Match.Clause matchingClause = null;
                    for (Match.Clause clause : match.clauses) {
                        newEnvironment = environment;

                        if (clause.constructorSymbol.equals(constructor.name)) {
                            matchingClause = clause;
                            break;
                        }
                    }

                    if (matchingClause == null)
                        throw new TypeException("no matching clause for constructor " + constructor.name);

                    PiTypeDecomposition constructorPiTypeDecomposition = getPiTypeDecomposition(constructor.definition);

                    if (matchingClause.argumentSymbols.size() != constructorPiTypeDecomposition.argumentTypes.size())
                        throw new TypeException("wrong number of arguments");

                    for (int i = 0; i < constructorPiTypeDecomposition.argumentTypes.size(); i++) {
                        Symbol symbol = matchingClause.argumentSymbols.get(i);
                        Expression argumentType = constructorPiTypeDecomposition.argumentTypes.get(i);
                        newEnvironment = newEnvironment.appendType(symbol, argumentType);
                    }

                    List<Expression> arguments = new ArrayList<>();

                    for (Symbol symbol : matchingClause.argumentSymbols) {
                        // TODO: is uniqueness working here?
                        arguments.add(new Variable(symbol));
                    }

                    Expression newExpression = new ConstructorCall(
                            inductiveType,
                            constructor.name,
                            arguments
                    );
                    newEnvironment = newEnvironment.appendScope(match.expressionSymbol, newExpression);
                    Expression specificReturnType = Interpreter.evaluateExpression(match.type, newEnvironment).expression();
                    checkType(matchingClause.expression, specificReturnType, newEnvironment);
                }

                result = returnType;
            }
        } else if (term instanceof Fix) {
            Fix fix = (Fix) term;
            Definition callingDefinition = null;
            Environment newEnvironment = environment;

            for (Definition definition : fix.definitions) {
                if (definition.name.equals(fix.symbol)) {
                    callingDefinition = definition;
                }

                newEnvironment = newEnvironment.appendType(definition.name, definition.type);
            }

            for (Definition definition : fix.definitions) {
                checkType(definition.definition, definition.type, newEnvironment);
            }

            if (callingDefinition == null) {
                throw new TypeException("function " + fix.symbol + " not defined in " + fix);
            }

            checkGuarded(fix, newEnvironment);

            result = callingDefinition.type;
        } else if (term instanceof Char) {
            result = new Variable(new Symbol("char"));
        }

        if (result == null) {
            System.out.println(Util.mapToStringWithNewlines(Map.of(
                    "term", term == null ? "null" : term,
                    "environment", environment
            )));
            throw new TypeException("Type Could not be Deduced");
        }

        // TODO: do we need this uncommented?
        // result = Interpreter.makeSymbolsUnique(result);
        return Interpreter.evaluateExpression(result, environment).expression();
    }

    private static class PiTypeDecomposition {
        private List<Symbol> arguments;
        private List<Expression> argumentTypes;
        private Expression bodyType;

        private PiTypeDecomposition() {
            this.arguments = new ArrayList<>();
            this.argumentTypes = new ArrayList<>();
            this.bodyType = null;
        }
    }

    // decomposes into M = {a_1:A_1}...{a_n:A_n}N with N not being a pi type,
    private static PiTypeDecomposition getPiTypeDecomposition(Expression expression) {
        return getPiTypeDecomposition(expression, new PiTypeDecomposition());
    }

    private static PiTypeDecomposition getPiTypeDecomposition(Expression expression, PiTypeDecomposition piTypeDecomposition) {
        if (expression instanceof PiType) {
            PiType piType = (PiType) expression;
            piTypeDecomposition.arguments.add(piType.variable);
            piTypeDecomposition.argumentTypes.add(piType.type);
            return getPiTypeDecomposition(piType.body, piTypeDecomposition);
        } else {
            piTypeDecomposition.bodyType = expression;
            return piTypeDecomposition;
        }
    }

    private static class ArgumentDecomposition {
        private List<Symbol> arguments;
        private List<Expression> argumentTypes;
        private Expression body;

        private ArgumentDecomposition() {
            this.arguments = new ArrayList<>();
            this.argumentTypes = new ArrayList<>();
            this.body = null;
        }
    }

    // given an expression M = \a_1:A_1->...\a_n:A_n->N with N not being an abstraction,
    // this function returns the list {a_1=A_1, ..., a_n=A_n}
    private static ArgumentDecomposition getArgumentDecomposition(Expression expression) {
        return getArgumentDecomposition(expression, new ArgumentDecomposition());
    }

    private static ArgumentDecomposition getArgumentDecomposition(Expression expression, ArgumentDecomposition argumentDecomposition) {
        if (expression instanceof Abstraction) {
            Abstraction abstraction = (Abstraction) expression;
            argumentDecomposition.arguments.add(abstraction.symbol);
            argumentDecomposition.argumentTypes.add(abstraction.type);
            Expression body = abstraction.body;
            return getArgumentDecomposition(body, argumentDecomposition);
        } else {
            argumentDecomposition.body = expression;
            return argumentDecomposition;
        }
    }

    private static class ApplicationDecomposition {
        private Expression function = null;
        private List<Expression> arguments = new ArrayList<>();
    }

    private static ApplicationDecomposition getApplicationDecomposition(Expression expression) {
        return getApplicationDecomposition(expression, new ApplicationDecomposition());
    }

    private static ApplicationDecomposition getApplicationDecomposition(Expression expression, ApplicationDecomposition applicationDecomposition) {
        if (expression instanceof Application) {
            // TODO: do we need to try to reduce function and arugment to make it more general?
            Application application = (Application) expression;
            applicationDecomposition.arguments.add(0, application.argument);
            return getApplicationDecomposition(application.function, applicationDecomposition);
        } else {
            applicationDecomposition.function = expression;
            return applicationDecomposition;
        }
    }

    private static void checkGuarded(Fix fix, Environment environment) throws TypeException {
        List<Integer> ks = new ArrayList<>();
        List<Symbol> xs = new ArrayList<>();

        for (Definition definition : fix.definitions) {
            ArgumentDecomposition argumentDecomposition = getArgumentDecomposition(definition.definition);

            if (!(argumentDecomposition.body instanceof Match))
                throw new TypeException("body of fix definition must be a match, not a " + argumentDecomposition.body.getClass().getSimpleName());

            Match match = (Match) argumentDecomposition.body;

            if (!(match.expression instanceof Variable))
                throw new TypeException("body of fix definition must match on an argument");

            Variable variable = (Variable) match.expression;
            Integer k = null;

            for (int i = 0; i < argumentDecomposition.arguments.size(); i++) {
                Symbol argument = argumentDecomposition.arguments.get(i);

                if (argument.equals(variable.symbol)) {
                    k = i;
                    break;
                }
            }

            if (k == null)
                throw new TypeException("" + variable + " isn't an argument to the fix function");

            ks.add(k);
            xs.add(variable.symbol);
        }

        for (int i = 0; i < fix.definitions.size(); i++) {
            Definition definition = fix.definitions.get(i);
            if (definition.name.equals(fix.symbol)) {
                checkGuarded(fix, environment, ks, xs, i, new ArrayList<>(), definition.definition);
                return;
            }
        }

        throw new TypeException("function " + fix.symbol + " not defined in " + fix);
    }

    static int depth = 0;

    private static void checkGuarded(Fix fix, Environment environment, List<Integer> ks, List<Symbol> xs, int i, List<Symbol> v, Expression expression) throws TypeException {
        depth++;

        String indent = "";
        for (int j = 0; j < depth; j++)
            indent += "--";
        indent += " ";

        String message = indent + "checking if " + expression + " is guarded";
        message = message.replaceAll("\n", "\n" + indent);
        // System.err.println(message);
        // TODO: figure this out

        /*boolean recursive = false;

        for (Definition definition : fix.definitions) {
            if (symbolAppearsIn(definition.name, expression)) {
                recursive = true;
            }
        }
        if (!recursive)
            return;*/

        if (expression instanceof Variable) {
            Variable variable = (Variable) expression;

            for (Definition definition : fix.definitions) {
                if (variable.symbol.equals(definition.name)) {
                    throw new TypeException("expression " + expression + " not guarded in " + fix);
                }
            }
        } else if (expression instanceof Abstraction) {
            Abstraction abstraction = (Abstraction) expression;
            checkGuarded(fix, environment, ks, xs, i, v, abstraction.type);
            checkGuarded(fix, environment, ks, xs, i, v, abstraction.body);
        } else if (expression instanceof PiType) {
            PiType piType = (PiType) expression;
            checkGuarded(fix, environment, ks, xs, i, v, piType.type);
            checkGuarded(fix, environment, ks, xs, i, v, piType.body);
        } else if (expression instanceof Fix) {
            Fix innerFix = (Fix) expression;
            Definition callingDefinition = null;

            for (Definition definition : innerFix.definitions) {
                if (definition.name.equals(innerFix.symbol)) {
                    callingDefinition = definition;
                }
            }

            if (callingDefinition == null) {
                throw new TypeException("function " + innerFix.symbol + " not defined in " + innerFix);
            }

            checkGuarded(fix, environment, ks, xs, i, v, callingDefinition.type);
            checkGuarded(fix, environment, ks, xs, i, v, callingDefinition.definition);
        } else if (expression instanceof InductiveType) {
            InductiveType inductiveType = (InductiveType) expression;

            for (Expression parameter : inductiveType.parameters) {
                checkGuarded(fix, environment, ks, xs, i, v, parameter);
            }

            if (inductiveType.isConcrete()) {
                for (Expression argument : inductiveType.arguments) {
                    checkGuarded(fix, environment, ks, xs, i, v, argument);
                }
            }
        } else if (expression instanceof ConstructorCall) {
            ConstructorCall constructorCall = (ConstructorCall) expression;
            checkGuarded(fix, environment, ks, xs, i, v, constructorCall.inductiveType);

            if (constructorCall.isConcrete()) {
                for (Expression argument : constructorCall.arguments) {
                    checkGuarded(fix, environment, ks, xs, i, v, argument);
                }
            }
        } else if (expression instanceof Match) {
            Match match = (Match) expression;
            Expression matchExpression = match.expression;
            ApplicationDecomposition applicationDecomposition = getApplicationDecomposition(matchExpression);

            if (applicationDecomposition.function instanceof Variable) {
                Variable variable = (Variable) applicationDecomposition.function;
                Symbol x = xs.get(i);

                if (variable.symbol.equals(x) || v.contains(variable.symbol)) {
                    checkGuarded(fix, environment, ks, xs, i, v, match.type);

                    for (Expression argument : applicationDecomposition.arguments) {
                        checkGuarded(fix, environment, ks, xs, i, v, argument);
                    }

                    for (Match.Clause clause : match.clauses) {
                        List<Symbol> newV = new ArrayList<>(v);
                        newV.addAll(clause.argumentSymbols);
                        checkGuarded(fix, environment, ks, xs, i, newV, clause.expression);

                        // TODO: figure this out
                        // do we need to check whether i is a recursive position,
                        // or does that just improve efficiency?
                    }

                    return;
                }
            }

            checkGuarded(fix, environment, ks, xs, i, v, match.type);
            checkGuarded(fix, environment, ks, xs, i, v, match.expression);

            for (Match.Clause clause : match.clauses) {
                checkGuarded(fix, environment, ks, xs, i, v, clause.expression);
            }
        } else if (expression instanceof Application) {
            ApplicationDecomposition applicationDecomposition = getApplicationDecomposition(expression);

            if (applicationDecomposition.function instanceof Variable) {
                Variable variable = (Variable) applicationDecomposition.function;

                for (Definition definition : fix.definitions) {
                    if (variable.symbol.equals(definition.name)) {
                        if (applicationDecomposition.arguments.size() < ks.get(i))
                            throw new TypeException("must pass at least " + ks.get(i) + " arguments to " + fix.definitions.get(i).name);

                        Expression recursive = applicationDecomposition.arguments.get(ks.get(i));
                        ApplicationDecomposition recursiveDecomposition = getApplicationDecomposition(recursive);

                        if (!(recursiveDecomposition.function instanceof Variable))
                            throw new TypeException("recursive argument must be application to variable");

                        Variable call = (Variable) recursiveDecomposition.function;

                        for (Expression argument : applicationDecomposition.arguments) {
                            checkGuarded(fix, environment, ks, xs, i, v, argument);
                        }

                        if (!v.contains(call.symbol))
                            throw new TypeException("v doesn't contain " + call.symbol);

                        return;
                    }
                }
            }

            checkGuarded(fix, environment, ks, xs, i, v, applicationDecomposition.function);

            for (Expression argument : applicationDecomposition.arguments) {
                checkGuarded(fix, environment, ks, xs, i, v, argument);
            }
        }

        depth--;
    }

    /*private static boolean symbolAppearsIn(Symbol symbol, Expression expression) {
        if (expression instanceof Variable) {
            Variable variable = (Variable) expression;
            return variable.symbol.equals(symbol);
        } else if (expression instanceof Application) {
            Application application = (Application) expression;
            return symbolAppearsIn(symbol, application.function)
                    || symbolAppearsIn(symbol, application.argument);
        } else if (expression instanceof Abstraction) {
            Abstraction abstraction = (Abstraction) expression;
            return symbolAppearsIn(symbol, abstraction.type)
                    || symbolAppearsIn(symbol, abstraction.body);
        } else if (expression instanceof PiType) {
            PiType piType = (PiType) expression;
            return symbolAppearsIn(symbol, piType.type)
                    || symbolAppearsIn(symbol, piType.body);
        } else if (expression instanceof Match) {
            Match match = (Match) expression;

            for (Match.Clause clause : match.clauses) {
                if (symbolAppearsIn(symbol, clause.expression))
                    return true;
            }

            return symbolAppearsIn(symbol, match.type)
                    || symbolAppearsIn(symbol, match.expression);
        } else if (expression instanceof InductiveType) {
            InductiveType inductiveType = (InductiveType) expression;

            for (Expression parameter : inductiveType.parameters) {
                if (symbolAppearsIn(symbol, parameter))
                    return true;
            }

            if (inductiveType.isConcrete()) {
                for (Expression argument : inductiveType.arguments) {
                    if (symbolAppearsIn(symbol, argument))
                        return true;
                }
            }

            return false;
        } else if (expression instanceof ConstructorCall) {
            ConstructorCall constructorCall = (ConstructorCall) expression;

            if (constructorCall.isConcrete()) {
                for (Expression argument : constructorCall.arguments) {
                    if (symbolAppearsIn(symbol, argument))
                        return true;
                }
            }

            return symbolAppearsIn(symbol, constructorCall.inductiveType);
        } else if (expression instanceof Fix) {
            Fix fix = (Fix) expression;

            for (Definition definition : fix.definitions) {
                if (symbolAppearsIn(symbol, definition.type))
                    return true;

                if (symbolAppearsIn(symbol, definition.definition))
                    return true;
            }

            return false;
        }
    }*/

    private static int umax(int level1, int level2) {
        if (level1 <= 2 && level2 <= 2) return 1;
        return Math.max(level1, level2);
    }

    private static <T> boolean allDistinct(Collection<T> collection) {
        return collection.stream().distinct().count() == collection.size();
    }

    private static boolean isSort(Expression expression) {
        if (expression instanceof Variable) {
            Variable variable = (Variable) expression;
            return variable.symbol.name.matches("Type\\d");
        }
        return false;
    }

    private static boolean isArity(Expression type) {
        if (isSort(type)) {
            return true;
        } else if (type instanceof PiType) {
            PiType piType = (PiType) type;
            return isArity(piType.body);
        }
        return false;
    }

    private static VType getArity(Value value) {
        if (value instanceof VType) {
            return (VType) value;
        } else if (value instanceof VPi) {
            VPi vPi = (VPi) value;
            Value dummyArgument = new VNeutral(new NFree(new Symbol("dummy")));
            Value bodyType = vPi.call(dummyArgument);
            return getArity(bodyType);
        }
        return null;
    }

    private static void checkTypeOfConstructorOf(Expression type, TypeDeclaration typeDeclaration, Environment environment) throws TypeException {
        Expression eval = Interpreter.evaluateExpression(type, environment).expression();

        if (eval instanceof InductiveType) {
            InductiveType inductiveType = (InductiveType) eval;

            if (inductiveType.isConcrete()) {
                if (inductiveType.parameters.size() != typeDeclaration.parameters.size())
                    throw new TypeException("" + type + " has wrong number of parameters to " + typeDeclaration);

                for (int i = 0; i < inductiveType.parameters.size(); i++) {
                    Parameter parameter = typeDeclaration.parameters.get(i);
                    Expression expression = (Expression) inductiveType.parameters.get(i);

                    if (!(expression instanceof Variable))
                        throw new TypeException("" + type + " not type of constructor of " + typeDeclaration);

                    Variable variable = (Variable) expression;

                    if (!(variable.symbol.equals(parameter.symbol)))
                        throw new TypeException("" + type + " parameters don't match " + typeDeclaration);
                }

                // TODO: sketchy? maybe not sketchy because of unique symbols?
                if (inductiveType.type.equals(typeDeclaration.name))
                    return;
            } else {
                System.out.println("not concrete");
            }
        } else if (eval instanceof PiType) {
            PiType piType = (PiType) eval;
            Environment newEnvironment = environment.appendType(piType.variable, piType.type);
            checkTypeOfConstructorOf(piType.body, typeDeclaration, newEnvironment);
            return;
        }

        throw new TypeException("" + type + " not type of constructor of " + typeDeclaration);
    }

    private static void checkPositivity(Expression expression, Symbol symbol, Environment environment) throws TypeException {
        ApplicationDecomposition applicationDecomposition = getApplicationDecomposition(expression);

        if (applicationDecomposition.function instanceof Variable) {
            Variable variable = (Variable) applicationDecomposition.function;

            if (variable.symbol.equals(symbol)) {
                boolean all = true;

                for (Expression argument : applicationDecomposition.arguments) {
                    if (occursIn(symbol, argument))
                        all = false;
                }

                if (all)
                    return;
            }
        } else if (applicationDecomposition.function instanceof InductiveType) {
            InductiveType inductiveType = (InductiveType) applicationDecomposition.function;

            if (inductiveType.type.equals(symbol)) {
                boolean all = true;

                for (Expression argument : applicationDecomposition.arguments) {
                    if (occursIn(symbol, argument))
                        all = false;
                }

                if (all)
                    return;
            }
        } else if (expression instanceof PiType) {
            PiType piType = (PiType) expression;
            checkStrictPositivity(piType.type, symbol, environment);
            checkPositivity(piType.body, symbol, environment);
            return;
        }

        throw new TypeException("" + expression + " doesn't satisfy positivity condition for " + symbol);
    }

    private static void checkStrictPositivity(Expression expression, Symbol symbol, Environment environment) throws TypeException {
        if (!occursIn(symbol, expression))
            return;

        ApplicationDecomposition applicationDecomposition = getApplicationDecomposition(expression);

        if (applicationDecomposition.function instanceof Variable) {
            Variable variable = (Variable) applicationDecomposition.function;

            if (variable.equals(symbol)) {
                boolean all = true;

                for (Expression argument : applicationDecomposition.arguments) {
                    if (occursIn(symbol, argument))
                        all = false;
                }

                if (all)
                    return;
            }
        } else if (applicationDecomposition.function instanceof InductiveType) {
            InductiveType inductiveType = (InductiveType) applicationDecomposition.function;

            if (inductiveType.type.equals(symbol)) {
                boolean all = true;

                for (Expression argument : applicationDecomposition.arguments) {
                    if (occursIn(symbol, argument))
                        all = false;
                }

                if (all)
                    return;
            }
        } else if (expression instanceof PiType) {
            PiType piType = (PiType) expression;

            if (!occursIn(symbol, piType.type)) {
                checkStrictPositivity(piType.body, symbol, environment);
                return;
            }
        } else if (expression instanceof InductiveType) {
            InductiveType inductiveType = (InductiveType) expression;
            TypeDeclaration typeDeclaration = environment.lookUpTypeDeclaration(inductiveType.type);

            if (inductiveType.isConcrete() &&
                    !typeDeclaration.isMutual() &&
                    typeDeclaration.parameters.size() == inductiveType.parameters.size()) {

                Environment newEnvironment = environment;

                for (int i = 0; i < typeDeclaration.parameters.size(); i++) {
                    Symbol name = typeDeclaration.parameters.get(i).symbol;
                    Expression parameter = inductiveType.parameters.get(i);
                    newEnvironment = newEnvironment.appendScope(name, parameter);
                }

                boolean all = true;

                for (Expression argument : inductiveType.arguments) {
                    if (occursIn(symbol, argument)) {
                        all = false;
                        break;
                    }
                }

                for (Constructor constructor : typeDeclaration.constructors) {
                    checkNestedPositivity(constructor.definition, symbol, newEnvironment);
                }

                if (all)
                    return;
            }
        }

        throw new TypeException("" + expression + " doesn't occur strictly positively in " + symbol);
    }

    private static void checkNestedPositivity(Expression expression, Symbol symbol, Environment environment) throws TypeException {
        ApplicationDecomposition applicationDecomposition = getApplicationDecomposition(expression);

        if (applicationDecomposition.function instanceof InductiveType) {
            InductiveType inductiveType = (InductiveType) applicationDecomposition.function;
            TypeDeclaration typeDeclaration = environment.lookUpTypeDeclaration(inductiveType.type);

            if (inductiveType.parameters.size() != typeDeclaration.parameters.size())
                throw new TypeException("" + expression + " doesn't satisfy nested positivity condition for " + symbol);

            if (inductiveType.isConcrete()) {
                for (Expression argument : inductiveType.arguments) {
                    if (occursIn(symbol, argument)) {
                        throw new TypeException("" + expression + " doesn't satisfy nested positivity condition for " + symbol);
                    }
                }

                return;
            }
        } else if (expression instanceof PiType) {
            PiType piType = (PiType) expression;
            checkStrictPositivity(piType.type, symbol, environment);
            checkNestedPositivity(piType.body, symbol, environment);
        }

        throw new TypeException("" + expression + " doesn't satisfy nested positivity condition for " + symbol);
    }

    private static boolean occursIn(Symbol symbol, Expression expression) {
        if (expression instanceof Abstraction) {
            Abstraction abstraction = (Abstraction) expression;
            return occursIn(symbol, abstraction.type)
                    || occursIn(symbol, abstraction.body);
        } else if (expression instanceof Application) {
            Application application = (Application) expression;
            return occursIn(symbol, application.function)
                    || occursIn(symbol, application.argument);
        } else if (expression instanceof ConstructorCall) {
            ConstructorCall constructorCall = (ConstructorCall) expression;

            if (constructorCall.isConcrete()) {
                for (Expression argument : constructorCall.arguments) {
                    if (occursIn(symbol, argument))
                        return true;
                }
            }

            return occursIn(symbol, constructorCall.inductiveType);
        } else if (expression instanceof Fix) {
            Fix fix = (Fix) expression;

            for (Definition definition : fix.definitions) {
                if (occursIn(symbol, definition.type))
                    return true;
                if (occursIn(symbol, definition.definition))
                    return true;
            }

            return false;
        } else if (expression instanceof InductiveType) {
            // TODO: this is probably sketchy and wrong

            InductiveType inductiveType = (InductiveType) expression;

            if (inductiveType.type.equals(symbol))
                return true;

            for (Expression parameter : inductiveType.parameters) {
                if (occursIn(symbol, parameter))
                    return true;
            }

            if (inductiveType.isConcrete()) {
                for (Expression argument : inductiveType.arguments) {
                    if (occursIn(symbol, argument))
                        return true;
                }
            }

            return false;
        } else if (expression instanceof Match) {
            Match match = (Match) expression;

            for (Match.Clause clause : match.clauses) {
                if (occursIn(symbol, clause.expression))
                    return true;
            }

            return occursIn(symbol, match.expression)
                    || occursIn(symbol, match.type);
        } else if (expression instanceof PiType) {
            PiType piType = (PiType) expression;
            return occursIn(symbol, piType.type) ||
                    occursIn(symbol, piType.body);
        } else if (expression instanceof Variable) {
            Variable variable = (Variable) expression;
            return variable.symbol.equals(symbol);
        }

        return false;
    }

    public static void checkInductiveDeclarationWellFormed(InductiveDeclaration inductiveDeclaration, Environment environment) throws TypeException {
        List<TypeDeclaration> tds = inductiveDeclaration.typeDeclarations;

        // k > 0
        if (tds.isEmpty())
            throw new TypeException("" + inductiveDeclaration + " isn't well-formed");

        // I_j and c_j all distinct names
        if (!allDistinct(tds.stream().map(td -> td.name).collect(Collectors.toList())))
            throw new TypeException("" + inductiveDeclaration + " isn't well-formed");

        if (!allDistinct(tds.stream().flatMap(td -> td.constructors.stream()).collect(Collectors.toList())))
            throw new TypeException("" + inductiveDeclaration + " isn't well-formed");

        // A_j is an arity of sort s_j and I_j ∉ E
        List<VType> arities = new ArrayList<>();
        for (TypeDeclaration td : tds) {
            if (!isArity(td.type))
                throw new TypeException("" + inductiveDeclaration + " isn't well-formed");
        }

        // C_jk is a type of constructor I_j which satisfies the positivity
        // condition for {I_j}
        for (TypeDeclaration td : tds) {
            for (Constructor c : td.constructors) {
                Environment newEnvironment = environment.appendInductive(inductiveDeclaration);
                checkTypeOfConstructorOf(c.definition, td, newEnvironment);

                // TODO: wtf
                /*for (TypeDeclaration td2 : tds) {
                    checkPositivity(c.definition, td2.name, newEnvironment);
                }*/
                checkPositivity(c.definition, td.name, newEnvironment);
            }
        }
    }
}
