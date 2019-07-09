package poulet.typing;

import poulet.ast.*;
import poulet.contextexpressions.*;
import poulet.exceptions.PouletException;
import poulet.inference.Inferer;
import poulet.interpreter.Evaluator;
import poulet.util.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class Checker {
    public static void checkType(ContextExpression term, ContextExpression type) throws PouletException {
        checkType(term.expression, type.expression, term.environment);
    }

    public static void checkType(ContextExpression term, Expression type) throws PouletException {
        checkType(term.expression, type, term.environment);
    }

    public static void checkType(Expression term, Expression type, Environment environment) throws PouletException {
        Expression deduced = deduceType(term, environment);
        deduced = Evaluator.reduce(deduced, environment);
        type = Evaluator.reduce(type, environment);

        // Subtyping check first
        if (deduced instanceof Type && type instanceof Type) {
            int deducedLevel = ((Type) deduced).level;
            int typeLevel = ((Type) type).level;

            if (deducedLevel <= typeLevel) {
                return;
            }
        }

        if (!Evaluator.convertible(deduced, type, environment)) {
            throw new PouletException("deduced type of " + term + " is " + deduced + ", doesn't match actual type " + type);
        }
    }

    public static Expression deduceType(Expression term, Environment environment) throws PouletException {
        return deduceType(term.contextExpression(environment)).expression;
    }

    public static ContextExpression deduceType(ContextExpression term) throws PouletException {
        if(term.environment == null)
            throw new PouletException("Cannot deduce type in null environment");

        ContextExpression result = term.accept(new ContextExpressionVisitor<>() {
            @Override
            public ContextExpression visit(ContextAbstraction abstraction) throws PouletException {
                ContextExpression abstractionType = Evaluator.reduce(abstraction.type);
                ContextExpression bodyType = deduceType(abstraction.body);
                return new ContextPiType(abstraction.symbol, abstractionType, bodyType, abstraction.inferable);
            }

            @Override
            public ContextExpression visit(ContextApplication application) throws PouletException {
                application = Inferer.fillImplicitArguments(application);

                ContextExpression functionType = deduceType(application.function);

                if (functionType instanceof ContextPiType) {
                    ContextPiType piType = (ContextPiType) functionType;
                    // TODO: Fix this
                    checkType(application.argument, piType.type);
                    return piType.body.substitute(piType.variable, application.argument);
                } else {
                    throw new PouletException("can't apply to " + application.function);
                }
            }

            @Override
            public ContextExpression visit(ContextCharLiteral charLiteral) throws PouletException {
                return new ContextVariable(new Symbol("char"), term.environment);
            }

            @Override
            public ContextExpression visit(ContextConstructorCall constructorCall) throws PouletException {
                Constructor constructor = term.environment.lookUpConstructor((ConstructorCall) constructorCall.expression);

                if (constructor == null) {
                    throw new PouletException("constructor not found");
                }

                TypeDeclaration typeDeclaration = term.environment.lookUpTypeDeclaration(constructorCall.inductiveType.type);

                if (typeDeclaration == null) {
                    throw new PouletException("type declaration not found");
                }

                if (constructorCall.inductiveType.parameters.size() != typeDeclaration.parameters.size()) {
                    throw new PouletException("wrong number of parameters");
                }

                Environment newEnvironment = term.environment;

                for (int i = 0; i < constructorCall.inductiveType.parameters.size(); i++) {
                    Expression parameterType = typeDeclaration.parameters.get(i).type;
                    ContextExpression parameter = constructorCall.inductiveType.parameters.get(i);
                    checkType(parameter, parameterType);
                    newEnvironment = newEnvironment.appendScope(typeDeclaration.parameters.get(i).symbol, parameter.expression);
                }

                if(constructorCall.isConcrete()) {
                    PiTypeDecomposition piTypeDecomposition = new PiTypeDecomposition(constructor.definition);

                    if (constructorCall.arguments.size() != piTypeDecomposition.argumentTypes.size()) {
                        throw new PouletException("wrong number of arguments");
                    }

                    for (int i = 0; i < constructorCall.arguments.size(); i++) {
                        Expression argumentType = piTypeDecomposition.argumentTypes.get(i);
                        ContextExpression argument = constructorCall.arguments.get(i);
                        checkType(argument.expression, argumentType, newEnvironment);
                    }

                    return Evaluator.reduce(piTypeDecomposition.bodyType.contextExpression(newEnvironment));
                } else {
                    return Evaluator.reduce(constructor.definition.contextExpression(newEnvironment));
                }
            }

            @Override
            public ContextExpression visit(ContextFix fix) throws PouletException {
                ContextDefinition callingDefinition = fix.getExported();
                Environment newEnvironment = term.environment;

                for (ContextDefinition definition : fix.definitions) {
                    newEnvironment = newEnvironment.appendType(definition.name, definition.type.expression);
                }

                for (ContextDefinition definition : fix.definitions) {
                    checkType(definition.definition, definition.type);
                }

                checkGuarded((Fix) fix.expression, newEnvironment);

                return callingDefinition.type;
            }

            @Override
            public ContextExpression visit(ContextInductiveType inductiveType) throws PouletException {
                TypeDeclaration typeDeclaration = term.environment.lookUpTypeDeclaration(inductiveType.type);

                if (typeDeclaration == null) {
                    throw new PouletException("type declaration not found");
                }

                if (inductiveType.parameters.size() != typeDeclaration.parameters.size()) {
                    throw new PouletException("wrong number of parameters");
                }

                Environment newEnvironment = term.environment;

                for (int i = 0; i < inductiveType.parameters.size(); i++) {
                    Expression parameterType = typeDeclaration.parameters.get(i).type;
                    ContextExpression parameter = inductiveType.parameters.get(i);
                    checkType(parameter, parameterType);
                    newEnvironment = newEnvironment.appendScope(typeDeclaration.parameters.get(i).symbol, parameter.expression);
                }

                return Evaluator.reduce(typeDeclaration.type.contextExpression(newEnvironment));
            }

            @Override
            public ContextExpression visit(ContextMatch match) throws PouletException {
                ContextExpression expressionType = deduceType(match.expression);

                if (expressionType instanceof ContextInductiveType) {
                    ContextInductiveType inductiveType = (ContextInductiveType) expressionType;
                    TypeDeclaration typeDeclaration = term.environment.lookUpTypeDeclaration(inductiveType.type);

                    ContextExpression returnType = Evaluator.reduce(match.type);

                    for (Constructor constructor : typeDeclaration.constructors) {
                        ContextMatch.Clause matchingClause = null;
                        for (ContextMatch.Clause clause : match.clauses) {
                            if (clause.constructorSymbol.equals(constructor.name)) {
                                matchingClause = clause;
                                break;
                            }
                        }
                        if (matchingClause == null)
                            throw new PouletException("no matching clause for constructor " + constructor.name);
                        checkType(matchingClause.expression, match.type);
                    }

                    return returnType;
                } else {
                    throw new PouletException("can only match on inductive type");
                }
            }

            @Override
            public ContextExpression visit(ContextPiType piType) throws PouletException {
                ContextExpression typeType = deduceType(piType.type);
                Symbol tempSymbol = new Symbol("temp").makeUnique();
                ContextExpression bodyType = deduceType(piType.body.substitute(tempSymbol, new Variable(tempSymbol)).appendType(tempSymbol, Evaluator.reduce(piType.type).expression));


                if (typeType.expression instanceof Sort && bodyType instanceof ContextProp) {
                    return new ContextProp(term.environment);
                } else if ((typeType instanceof ContextProp || typeType.expression instanceof Set) && bodyType instanceof ContextSet) {
                    return new ContextSet(term.environment);
                } else if (typeType instanceof ContextType && bodyType instanceof ContextType) {
                    int typeLevel = ((ContextType) typeType).level;
                    int bodyLevel = ((ContextType) bodyType).level;
                    return new ContextType(Math.max(typeLevel, bodyLevel), term.environment);
                } else {
                    throw new PouletException("pi type must qualify over a sort");
                }
            }

            @Override
            public ContextExpression visit(ContextProp prop) throws PouletException {
                return new ContextType(1, term.environment);
            }

            @Override
            public ContextExpression visit(ContextSet set) throws PouletException {
                return new ContextType(1, term.environment);
            }

            @Override
            public ContextExpression visit(ContextType type) throws PouletException {
                return new ContextType(type.level + 1, term.environment);
            }

            @Override
            public ContextExpression visit(ContextVariable variable) throws PouletException {
                Expression variableType = term.environment.lookUpType(variable.symbol);
                Expression definition = term.environment.lookUpScope(variable.symbol);

                if (variableType != null) {
                    return variableType.contextExpression(term.environment);
                } else if (definition != null) {
                    return deduceType(definition.contextExpression(term.environment));
                } else {
                    throw new PouletException("unknown identifier " + variable);
                }
            }
        });

        return Evaluator.reduce(result);
    }

    private static void checkGuarded(Fix fix, Environment environment) throws PouletException {
        List<Integer> ks = new ArrayList<>();
        List<Symbol> xs = new ArrayList<>();

        for (Definition definition : fix.definitions) {
            ArgumentDecomposition argumentDecomposition = new ArgumentDecomposition(definition.definition);

            if (!(argumentDecomposition.body instanceof Match))
                throw new PouletException("body of fix definition must be a match, not a " + argumentDecomposition.body.getClass().getSimpleName());

            Match match = (Match) argumentDecomposition.body;

            if (!(match.expression instanceof Variable))
                throw new PouletException("body of fix definition must match on an argument");

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
                throw new PouletException("" + variable + " isn't an argument to the fix function");

            ks.add(k);
            xs.add(variable.symbol);
        }

        for (int i = 0; i < fix.definitions.size(); i++) {
            Definition definition = fix.definitions.get(i);
            if (definition.name.equals(fix.export)) {
                checkGuarded(fix, environment, ks, xs, i, new ArrayList<>(), definition.definition);
                return;
            }
        }

        throw new PouletException("function " + fix.export + " not defined in " + fix);
    }

    private static void checkGuarded(Fix fix, Environment environment, List<Integer> ks, List<Symbol> xs, int i, List<Symbol> v, Expression expression) throws PouletException {
        if (expression instanceof Variable) {
            Variable variable = (Variable) expression;

            for (Definition definition : fix.definitions) {
                if (variable.symbol.equals(definition.name)) {
                    throw new PouletException("expression " + expression + " not guarded in " + fix);
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
                if (definition.name.equals(innerFix.export)) {
                    callingDefinition = definition;
                }
            }

            if (callingDefinition == null) {
                throw new PouletException("function " + innerFix.export + " not defined in " + innerFix);
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
            ApplicationDecomposition applicationDecomposition = new ApplicationDecomposition(matchExpression);

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
            ApplicationDecomposition applicationDecomposition = new ApplicationDecomposition(expression);

            if (applicationDecomposition.function instanceof Variable) {
                Variable variable = (Variable) applicationDecomposition.function;

                for (Definition definition : fix.definitions) {
                    if (variable.symbol.equals(definition.name)) {
                        if (applicationDecomposition.arguments.size() < ks.get(i))
                            throw new PouletException("must pass at least " + ks.get(i) + " arguments to " + fix.definitions.get(i).name);

                        Expression recursive = applicationDecomposition.arguments.get(ks.get(i));
                        ApplicationDecomposition recursiveDecomposition = new ApplicationDecomposition(recursive);

                        if (!(recursiveDecomposition.function instanceof Variable))
                            throw new PouletException("recursive argument must be application to variable");

                        Variable call = (Variable) recursiveDecomposition.function;

                        for (Expression argument : applicationDecomposition.arguments) {
                            checkGuarded(fix, environment, ks, xs, i, v, argument);
                        }

                        if (!v.contains(call.symbol)) {
                            throw new PouletException("v doesn't contain " + call.symbol);
                        }

                        return;
                    }
                }
            }

            checkGuarded(fix, environment, ks, xs, i, v, applicationDecomposition.function);

            for (Expression argument : applicationDecomposition.arguments) {
                checkGuarded(fix, environment, ks, xs, i, v, argument);
            }
        }
    }

    private static <T> boolean allDistinct(Collection<T> collection) {
        return collection.stream().distinct().count() == collection.size();
    }

    private static boolean isArity(Expression type) {
        if (type instanceof Sort) {
            return true;
        } else if (type instanceof PiType) {
            PiType piType = (PiType) type;
            return isArity(piType.body);
        }
        return false;
    }

    private static void checkTypeOfConstructorOf(Expression type, TypeDeclaration typeDeclaration, Environment environment) throws PouletException {
        Expression eval = Evaluator.reduce(type, environment);

        if (eval instanceof InductiveType) {
            InductiveType inductiveType = (InductiveType) eval;

            if (inductiveType.isConcrete()) {
                if (inductiveType.parameters.size() != typeDeclaration.parameters.size())
                    throw new PouletException("" + type + " has wrong number of parameters to " + typeDeclaration);

                for (int i = 0; i < inductiveType.parameters.size(); i++) {
                    Parameter parameter = typeDeclaration.parameters.get(i);
                    Expression expression = inductiveType.parameters.get(i);

                    if (!(expression instanceof Variable))
                        throw new PouletException("" + type + " not type of constructor of " + typeDeclaration);

                    Variable variable = (Variable) expression;

                    if (!(variable.symbol.equals(parameter.symbol)))
                        throw new PouletException("" + type + " parameters don't match " + typeDeclaration);
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

        throw new PouletException("" + type + " not type of constructor of " + typeDeclaration);
    }

    private static void checkPositivity(Expression expression, Symbol symbol, Environment environment) throws PouletException {
        ApplicationDecomposition applicationDecomposition = new ApplicationDecomposition(expression);

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

        throw new PouletException("" + expression + " doesn't satisfy positivity condition for " + symbol);
    }

    private static void checkStrictPositivity(Expression expression, Symbol symbol, Environment environment) throws PouletException {
        if (!occursIn(symbol, expression))
            return;

        ApplicationDecomposition applicationDecomposition = new ApplicationDecomposition(expression);

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
                    typeDeclaration.constructors.size() <= 1 &&
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

        throw new PouletException("" + expression + " doesn't occur strictly positively in " + symbol);
    }

    private static void checkNestedPositivity(Expression expression, Symbol symbol, Environment environment) throws PouletException {
        ApplicationDecomposition applicationDecomposition = new ApplicationDecomposition(expression);

        if (applicationDecomposition.function instanceof InductiveType) {
            InductiveType inductiveType = (InductiveType) applicationDecomposition.function;
            TypeDeclaration typeDeclaration = environment.lookUpTypeDeclaration(inductiveType.type);

            if (inductiveType.parameters.size() != typeDeclaration.parameters.size())
                throw new PouletException("" + expression + " doesn't satisfy nested positivity condition for " + symbol);

            if (inductiveType.isConcrete()) {
                for (Expression argument : inductiveType.arguments) {
                    if (occursIn(symbol, argument)) {
                        throw new PouletException("" + expression + " doesn't satisfy nested positivity condition for " + symbol);
                    }
                }

                return;
            }
        } else if (expression instanceof PiType) {
            PiType piType = (PiType) expression;
            checkStrictPositivity(piType.type, symbol, environment);
            checkNestedPositivity(piType.body, symbol, environment);
        }

        throw new PouletException("" + expression + " doesn't satisfy nested positivity condition for " + symbol);
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

    public static void checkInductiveDeclarationWellFormed(InductiveDeclaration inductiveDeclaration, Environment environment) throws PouletException {
        List<TypeDeclaration> tds = inductiveDeclaration.typeDeclarations;

        // k > 0
        if (tds.isEmpty())
            throw new PouletException("" + inductiveDeclaration + " isn't well-formed");

        // I_j and c_j all distinct names
        if (!allDistinct(tds.stream().map(td -> td.name).collect(Collectors.toList())))
            throw new PouletException("" + inductiveDeclaration + " isn't well-formed");

        if (!allDistinct(tds.stream().flatMap(td -> td.constructors.stream()).collect(Collectors.toList())))
            throw new PouletException("" + inductiveDeclaration + " isn't well-formed");

        // A_j is an arity of sort s_j and I_j ∉ E
        for (TypeDeclaration td : tds) {
            if (!isArity(td.type))
                throw new PouletException("" + inductiveDeclaration + " isn't well-formed");
        }

        // C_jk is a type of constructor I_j which satisfies the positivity
        // condition for {I_j}
        for (TypeDeclaration td : tds) {
            for (Constructor c : td.constructors) {
                Environment newEnvironment = environment.appendInductive(inductiveDeclaration);
                checkTypeOfConstructorOf(c.definition, td, newEnvironment);

                // TODO: does this work?
                /*for (TypeDeclaration td2 : tds) {
                    checkPositivity(c.definition, td2.name, newEnvironment);
                }*/
                checkPositivity(c.definition, td.name, newEnvironment);
            }
        }
    }
}
