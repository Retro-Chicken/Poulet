package poulet.kernel;

import poulet.PouletException;
import poulet.kernel.ast.*;
import poulet.kernel.context.GlobalContext;
import poulet.kernel.context.LocalContext;
import poulet.kernel.decomposition.ApplicationDecomposition;
import poulet.kernel.decomposition.ArgumentDecomposition;
import poulet.kernel.decomposition.ProdDecomposition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

class Checker {
    static void checkType(Expression term, Expression type, LocalContext context) {
        Expression deduced = deduceType(term, context);
        deduced = Reducer.reduce(deduced, context);
        type = Reducer.reduce(type, context);

        // check subtyping for Type(n)
        if (deduced instanceof Type && type instanceof Type) {
            int deducedLevel = ((Type) deduced).level;
            int typeLevel = ((Type) type).level;

            if (deducedLevel <= typeLevel) {
                return;
            }
        }

        if (!Reducer.convertible(deduced, type, context)) {
            throw new PouletException("deduced type of " + term + " is " + deduced + ", doesn't match actual type " + type);
        }
    }

    static Expression deduceType(Expression term, LocalContext context) {
        return term.accept(new ExpressionVisitor<>() {
            @Override
            public Expression visit(Abstraction abstraction) {
                Expression abstractionType = Reducer.reduce(abstraction.argumentType, context);
                LocalContext boundContext = new LocalContext(context);
                boundContext.assume(abstraction.argumentSymbol, abstractionType);
                Expression bodyType = deduceType(abstraction.body, boundContext);
                return new Prod(abstraction.argumentSymbol, abstractionType, bodyType);
            }

            @Override
            public Expression visit(Application application) {
                return deduceApplicationType(application, context);
            }

            @Override
            public Expression visit(ConstructorCall constructorCall) {
                TypeDeclaration.Constructor constructor = context.getConstructor(constructorCall);

                if (constructor == null) {
                    throw new PouletException("constructor " + constructorCall.constructor + " for " + constructorCall.inductiveType + " not found");
                }

                TypeDeclaration typeDeclaration = context.getTypeDeclaration(constructorCall.inductiveType);

                if (typeDeclaration == null) {
                    throw new PouletException("type declaration " + constructorCall.inductiveType + " not found");
                }

                if (constructorCall.parameters.size() != typeDeclaration.parameters.size()) {
                    throw new PouletException("wrong number of parameters for " + constructorCall.inductiveType);
                }

                LocalContext parameterContext = new LocalContext(context);

                for (int i = 0; i < constructorCall.parameters.size(); i++) {
                    Expression parameterType = typeDeclaration.parameters.get(i).type;
                    Expression parameter = constructorCall.parameters.get(i);
                    checkType(parameter, parameterType, parameterContext);
                    parameterContext.define(typeDeclaration.parameters.get(i).name, parameter);
                }

                ProdDecomposition prodDecomposition = new ProdDecomposition(constructor.definition);
                int totalArguments = prodDecomposition.arguments.size();
                int argumentsPassed = constructorCall.arguments.size();
                prodDecomposition.arguments = prodDecomposition.arguments.subList(argumentsPassed, totalArguments);
                return Reducer.reduce(prodDecomposition.expression(), parameterContext);
            }

            @Override
            public Expression visit(Fix fix) {
                Fix.Clause mainClause = fix.getMainClause();
                LocalContext fixContext = new LocalContext(context);

                for (Fix.Clause clause : fix.clauses) {
                    fixContext.assume(clause.symbol, clause.type);
                }

                for (Fix.Clause clause : fix.clauses) {
                    checkType(clause.definition, clause.type, fixContext);
                }

                checkGuarded(fix, fixContext);

                return mainClause.type;
            }

            @Override
            public Expression visit(InductiveType inductiveType) {
                TypeDeclaration typeDeclaration = context.getTypeDeclaration(inductiveType.inductiveType);

                if (typeDeclaration == null) {
                    throw new PouletException("type declaration not found");
                }

                if (inductiveType.parameters.size() != typeDeclaration.parameters.size()) {
                    throw new PouletException("wrong number of parameters");
                }

                LocalContext parameterContext = new LocalContext(context);

                for (int i = 0; i < inductiveType.parameters.size(); i++) {
                    Expression parameterType = typeDeclaration.parameters.get(i).type;
                    Expression parameter = inductiveType.parameters.get(i);
                    checkType(parameter, parameterType, parameterContext);
                    parameterContext.define(typeDeclaration.parameters.get(i).name, parameter);
                }

                return Reducer.reduce(typeDeclaration.type, parameterContext);
            }

            @Override
            public Expression visit(Match match) {
                Expression expressionType = deduceType(match.expression, context);

                if (expressionType instanceof InductiveType) {
                    InductiveType inductiveType = (InductiveType) expressionType;
                    TypeDeclaration typeDeclaration = context.getTypeDeclaration(inductiveType.inductiveType);

                    Expression returnType = Reducer.reduce(match.type, context);

                    for (TypeDeclaration.Constructor constructor : typeDeclaration.constructors) {
                        Match.Clause matchingClause = null;
                        for (Match.Clause clause : match.clauses) {
                            if (clause.constructor.equals(constructor.name)) {
                                matchingClause = clause;
                                break;
                            }
                        }

                        if (matchingClause == null) {
                            throw new PouletException("no matching clause for constructor " + constructor.name);
                        }

                        ProdDecomposition prodDecomposition = new ProdDecomposition(constructor.definition);
                        LocalContext clauseContext = new LocalContext(context);

                        for (int i = 0; i < matchingClause.argumentSymbols.size(); i++) {
                            Symbol argumentSymbol = matchingClause.argumentSymbols.get(i);
                            Expression argumentType = prodDecomposition.argumentTypes.get(i);
                            clauseContext.assume(argumentSymbol, argumentType);
                        }

                        checkType(matchingClause.expression, match.type, clauseContext);
                    }

                    return returnType;
                } else {
                    throw new PouletException("can only match on inductive type");
                }
            }

            @Override
            public Expression visit(MetaVar metaVar) {
                // TODO: how tf does this work
                return null;
            }

            @Override
            public Expression visit(Prod prod) {
                Expression argumentSort = deduceType(prod.argumentType, context);
                Expression bodySort = deduceType(prod.bodyType, context);


                if (argumentSort instanceof Sort && bodySort instanceof Prop) {
                    return new Prop();
                } else if ((argumentSort instanceof Prop || bodySort instanceof Set) && bodySort instanceof Set) {
                    return new Set();
                } else if (argumentSort instanceof Type && bodySort instanceof Type) {
                    int typeLevel = ((Type) argumentSort).level;
                    int bodyLevel = ((Type) bodySort).level;
                    return new Type(Math.max(typeLevel, bodyLevel));
                } else {
                    throw new PouletException("pi type must qualify over a sort");
                }
            }

            @Override
            public Expression visit(Prop sort) {
                return new Type(1);
            }

            @Override
            public Expression visit(Set set) {
                return new Type(1);
            }

            @Override
            public Expression visit(Type type) {
                return new Type(type.level + 1);
            }

            @Override
            public Expression visit(Var var) {
                Expression variableType = context.getAssumption(var.symbol);
                Expression definition = context.getDefinition(var.symbol);

                if (variableType != null) {
                    return variableType;
                } else if (definition != null) {
                    return deduceType(definition, context);
                } else {
                    throw new PouletException("unknown identifier " + var);
                }
            }
        });
    }

    private static Expression deduceApplicationType(Application application, LocalContext context) {
        Expression functionType = deduceType(application.function, context);

        if (functionType instanceof Prod) {
            Prod prod = (Prod) functionType;
            checkType(application.argument, prod.argumentType, context);
            return prod.bodyType.substitute(prod.argumentSymbol, application.argument);
        } else {
            throw new PouletException("can't apply to " + application.function);
        }
    }

    static void checkWellFormed(InductiveDeclaration inductiveDeclaration) {
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

        LocalContext newContext = new LocalContext(new GlobalContext());
        newContext.declareInductive(inductiveDeclaration);

        // C_jk is a type of constructor I_j which satisfies the positivity
        // condition for {I_j}
        for (TypeDeclaration td : tds) {
            for (TypeDeclaration.Constructor c : td.constructors) {
                checkTypeOfConstructorOf(c.definition, td);
                checkPositivity(c.definition, td.name, newContext);
            }
        }
    }

    private static void checkGuarded(Fix fix, LocalContext context) {
        List<Integer> ks = new ArrayList<>();
        List<Symbol> xs = new ArrayList<>();

        for (Fix.Clause clause : fix.clauses) {
            ArgumentDecomposition argumentDecomposition = new ArgumentDecomposition(clause.definition);

            if (!(argumentDecomposition.body instanceof Match))
                throw new PouletException("body of fix definition must be a match, not a " + argumentDecomposition.body.getClass().getSimpleName());

            Match match = (Match) argumentDecomposition.body;

            if (!(match.expression instanceof Var))
                throw new PouletException("body of fix definition must match on an argument");

            Var var = (Var) match.expression;
            Integer k = null;

            for (int i = 0; i < argumentDecomposition.arguments.size(); i++) {
                Symbol argument = argumentDecomposition.arguments.get(i);

                if (argument.equals(var.symbol)) {
                    k = i;
                    break;
                }
            }

            if (k == null)
                throw new PouletException("" + var + " isn't an argument to the fix function");

            ks.add(k);
            xs.add(var.symbol);
        }

        for (int i = 0; i < fix.clauses.size(); i++) {
            Fix.Clause clause = fix.clauses.get(i);
            if (clause.symbol.equals(fix.mainSymbol)) {
                checkGuarded(fix, context, ks, xs, i, new ArrayList<>(), clause.definition);
                return;
            }
        }

        throw new PouletException("function " + fix.mainSymbol + " not defined in " + fix);
    }

    private static void checkGuarded(Fix fix, LocalContext context, List<Integer> ks, List<Symbol> xs, int i, List<Symbol> v, Expression expression) {
        if (expression instanceof Var) {
            Var var = (Var) expression;

            for (Fix.Clause clause : fix.clauses) {
                if (var.symbol.equals(clause.symbol)) {
                    throw new PouletException("expression " + expression + " not guarded in " + fix);
                }
            }
        } else if (expression instanceof Abstraction) {
            Abstraction abstraction = (Abstraction) expression;
            checkGuarded(fix, context, ks, xs, i, v, abstraction.argumentType);
            checkGuarded(fix, context, ks, xs, i, v, abstraction.body);
        } else if (expression instanceof Prod) {
            Prod prod = (Prod) expression;
            checkGuarded(fix, context, ks, xs, i, v, prod.argumentType);
            checkGuarded(fix, context, ks, xs, i, v, prod.bodyType);
        } else if (expression instanceof Fix) {
            Fix innerFix = (Fix) expression;
            Fix.Clause mainClause = innerFix.getMainClause();
            checkGuarded(fix, context, ks, xs, i, v, mainClause.type);
            checkGuarded(fix, context, ks, xs, i, v, mainClause.definition);
        } else if (expression instanceof InductiveType) {
            InductiveType inductiveType = (InductiveType) expression;

            for (Expression parameter : inductiveType.parameters) {
                checkGuarded(fix, context, ks, xs, i, v, parameter);
            }

            for (Expression argument : inductiveType.arguments) {
                checkGuarded(fix, context, ks, xs, i, v, argument);
            }
        } else if (expression instanceof ConstructorCall) {
            ConstructorCall constructorCall = (ConstructorCall) expression;

            for (Expression parameter : constructorCall.parameters) {
                checkGuarded(fix, context, ks, xs, i, v, parameter);
            }

            for (Expression argument : constructorCall.arguments) {
                checkGuarded(fix, context, ks, xs, i, v, argument);
            }
        } else if (expression instanceof Match) {
            Match match = (Match) expression;
            Expression matchExpression = match.expression;
            ApplicationDecomposition applicationDecomposition = new ApplicationDecomposition(matchExpression);

            if (applicationDecomposition.function instanceof Var) {
                Var var = (Var) applicationDecomposition.function;
                Symbol x = xs.get(i);

                if (var.symbol.equals(x) || v.contains(var.symbol)) {
                    checkGuarded(fix, context, ks, xs, i, v, match.type);

                    for (Expression argument : applicationDecomposition.arguments) {
                        checkGuarded(fix, context, ks, xs, i, v, argument);
                    }

                    for (Match.Clause clause : match.clauses) {
                        List<Symbol> newV = new ArrayList<>(v);
                        newV.addAll(clause.argumentSymbols);
                        checkGuarded(fix, context, ks, xs, i, newV, clause.expression);

                        // TODO: figure this out
                        // do we need to check whether i is a recursive position,
                        // or does that just improve efficiency?
                    }

                    return;
                }
            }

            checkGuarded(fix, context, ks, xs, i, v, match.type);
            checkGuarded(fix, context, ks, xs, i, v, match.expression);

            for (Match.Clause clause : match.clauses) {
                checkGuarded(fix, context, ks, xs, i, v, clause.expression);
            }
        } else if (expression instanceof Application) {
            ApplicationDecomposition applicationDecomposition = new ApplicationDecomposition(expression);

            if (applicationDecomposition.function instanceof Var) {
                Var var = (Var) applicationDecomposition.function;

                for (Fix.Clause clause : fix.clauses) {
                    if (var.symbol.equals(clause.symbol)) {
                        if (applicationDecomposition.arguments.size() < ks.get(i))
                            throw new PouletException("must pass at least " + ks.get(i) + " arguments to " + fix.clauses.get(i).symbol);

                        Expression recursive = applicationDecomposition.arguments.get(ks.get(i));
                        ApplicationDecomposition recursiveDecomposition = new ApplicationDecomposition(recursive);

                        if (!(recursiveDecomposition.function instanceof Var))
                            throw new PouletException("recursive argument must be application to variable");

                        Var call = (Var) recursiveDecomposition.function;

                        for (Expression argument : applicationDecomposition.arguments) {
                            checkGuarded(fix, context, ks, xs, i, v, argument);
                        }

                        if (!v.contains(call.symbol)) {
                            throw new PouletException("v doesn't contain " + call.symbol);
                        }

                        return;
                    }
                }
            }

            checkGuarded(fix, context, ks, xs, i, v, applicationDecomposition.function);

            for (Expression argument : applicationDecomposition.arguments) {
                checkGuarded(fix, context, ks, xs, i, v, argument);
            }
        }
    }

    private static <T> boolean allDistinct(Collection<T> collection) {
        return collection.stream().distinct().count() == collection.size();
    }

    private static boolean isArity(Expression type) {
        if (type instanceof Sort) {
            return true;
        } else if (type instanceof Prod) {
            Prod prod = (Prod) type;
            return isArity(prod.bodyType);
        }
        return false;
    }

    private static void checkTypeOfConstructorOf(Expression type, TypeDeclaration typeDeclaration) {
        ProdDecomposition prodDecomposition = new ProdDecomposition(type);
        ApplicationDecomposition applicationDecomposition = new ApplicationDecomposition(prodDecomposition.bodyType);

        if (applicationDecomposition.function instanceof InductiveType) {
            InductiveType inductiveType = (InductiveType) applicationDecomposition.function;

            for (int i = 0; i < inductiveType.parameters.size(); i++) {
                TypeDeclaration.Parameter parameter = typeDeclaration.parameters.get(i);
                Expression expression = inductiveType.parameters.get(i);

                if (!(expression instanceof Var))
                    throw new PouletException("" + type + " not type of constructor of " + typeDeclaration);

                Var var = (Var) expression;

                if (!(var.symbol.equals(parameter.name)))
                    throw new PouletException("" + type + " parameters don't match " + typeDeclaration);
            }

            if (inductiveType.inductiveType.equals(typeDeclaration.name))
                return;
        }

        throw new PouletException("" + type + " not type of constructor of " + typeDeclaration);
    }

    private static void checkPositivity(Expression expression, Symbol symbol, LocalContext context) {
        ApplicationDecomposition applicationDecomposition = new ApplicationDecomposition(expression);

        if (applicationDecomposition.function instanceof Var) {
            Var var = (Var) applicationDecomposition.function;

            if (var.symbol.equals(symbol)) {
                boolean all = true;

                for (Expression argument : applicationDecomposition.arguments) {
                    if (argument.occurs(symbol)) {
                        all = false;
                    }
                }

                if (all) {
                    return;
                }
            }
        } else if (applicationDecomposition.function instanceof InductiveType) {
            InductiveType inductiveType = (InductiveType) applicationDecomposition.function;

            if (inductiveType.inductiveType.equals(symbol)) {
                boolean all = true;

                for (Expression argument : applicationDecomposition.arguments) {
                    if (argument.occurs(symbol))
                        all = false;
                }

                if (all)
                    return;
            }
        } else if (expression instanceof Prod) {
            Prod prod = (Prod) expression;
            checkStrictPositivity(prod.argumentType, symbol, context);
            checkPositivity(prod.bodyType, symbol, context);
            return;
        }

        throw new PouletException("" + expression + " doesn't satisfy positivity condition for " + symbol);
    }

    private static void checkStrictPositivity(Expression expression, Symbol symbol, LocalContext context) {
        if (!expression.occurs(symbol)) {
            return;
        }

        ApplicationDecomposition applicationDecomposition = new ApplicationDecomposition(expression);

        if (applicationDecomposition.function instanceof Var) {
            Var var = (Var) applicationDecomposition.function;

            if (var.symbol.equals(symbol)) {
                boolean all = true;

                for (Expression argument : applicationDecomposition.arguments) {
                    if (argument.occurs(symbol)) {
                        all = false;
                    }
                }

                if (all) {
                    return;
                }
            }
        } else if (applicationDecomposition.function instanceof InductiveType) {
            InductiveType inductiveType = (InductiveType) applicationDecomposition.function;
            TypeDeclaration typeDeclaration = context.getTypeDeclaration(inductiveType.inductiveType);
            boolean isMutual = typeDeclaration.inductiveDeclaration.typeDeclarations.size() == 1;

            if (!isMutual) {
                boolean all = true;

                for (Expression argument : applicationDecomposition.arguments) {
                    if (argument.occurs(symbol)) {
                        all = false;
                    }
                }

                if (all) {
                    for (TypeDeclaration.Constructor constructor : typeDeclaration.constructors) {
                        Expression substituted = constructor.definition;

                        for (int i = 0; i < inductiveType.parameters.size(); i++) {
                            substituted = substituted.substitute(
                                    typeDeclaration.parameters.get(i).name,
                                    inductiveType.parameters.get(i)
                            );
                        }

                        checkNestedPositivity(substituted, symbol, context);
                    }

                    return;
                }
            }
        } else if (expression instanceof Prod) {
            Prod prod = (Prod) expression;

            if (!prod.argumentType.occurs(symbol)) {
                checkStrictPositivity(prod.bodyType, symbol, context);
            }
        }

        throw new PouletException("strict positivity not satisfied");
    }

    private static void checkNestedPositivity(Expression expression, Symbol symbol, LocalContext context) {
        ApplicationDecomposition applicationDecomposition = new ApplicationDecomposition(expression);

        if (applicationDecomposition.function instanceof InductiveType) {
            InductiveType inductiveType = (InductiveType) applicationDecomposition.function;
            TypeDeclaration typeDeclaration = context.getTypeDeclaration(inductiveType.inductiveType);

            if (inductiveType.parameters.size() != typeDeclaration.parameters.size())
                throw new PouletException("" + expression + " doesn't satisfy nested positivity condition for " + symbol);

            for (Expression argument : inductiveType.arguments) {
                if (argument.occurs(symbol)) {
                    throw new PouletException("" + expression + " doesn't satisfy nested positivity condition for " + symbol);
                }
            }

            return;
        } else if (expression instanceof Prod) {
            Prod prod = (Prod) expression;
            checkStrictPositivity(prod.argumentType, symbol, context);
            checkNestedPositivity(prod.bodyType, symbol, context);
        }

        throw new PouletException("" + expression + " doesn't satisfy nested positivity condition for " + symbol);
    }
}