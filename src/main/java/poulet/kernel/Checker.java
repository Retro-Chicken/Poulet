package poulet.kernel;

import poulet.PouletException;
import poulet.kernel.ast.*;
import poulet.kernel.context.GlobalContext;
import poulet.kernel.context.LocalContext;
import poulet.kernel.decomposition.ApplicationDecomposition;
import poulet.kernel.decomposition.AbstractionDecomposition;
import poulet.kernel.decomposition.ProdDecomposition;
import poulet.util.StringUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
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
            throw new PouletException(
                    StringUtil.mapToStringWithNewlines(Map.of(
                            "term", term,
                            "deduced", deduced,
                            "expected", type,
                            "context", context
                    ))
            );
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
                TypeDeclaration.Constructor constructor = context.getConstructor(constructorCall.inductiveType, constructorCall.constructor);

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

                checkGuarded(fix);

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
                    Expression expressionSort = deduceType(inductiveType, context);
                    Expression returnType = convertMatchTypeToAbstraction(match, inductiveType, context);
                    Expression returnSort = deduceType(returnType, context);

                    if (!isAllowedEliminationSort(inductiveType, expressionSort, returnSort, context)) {
                        throw new PouletException("elimination sort [" + inductiveType + " : " + expressionSort + " | " + returnSort + "] not allowed");
                    }

                    for (Match.Clause clause : match.clauses) {
                        Expression constructorDefinition = context.getConstructor(inductiveType.inductiveType, clause.constructor).definition;
                        Expression abstraction = convertMatchBranchToAbstraction(inductiveType, clause, constructorDefinition, context);
                        Expression clauseType = getMatchClauseType(inductiveType, clause.constructor, returnType, context);
                        checkType(abstraction, clauseType, context);
                    }

                    List<Expression> arguments = new ArrayList<>(inductiveType.arguments);
                    arguments.add(match.expression);

                    return Reducer.reduce(
                            new ApplicationDecomposition(
                                    returnType,
                                    arguments
                            ).expression(),
                            context
                    );
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
                LocalContext boundContext = new LocalContext(context);
                boundContext.assume(prod.argumentSymbol, prod.argumentType);
                Expression bodySort = deduceType(prod.bodyType, boundContext);


                if (argumentSort instanceof Sort && bodySort instanceof Prop) {
                    return new Prop();
                } else if ((argumentSort instanceof Prop || argumentSort instanceof Set) && bodySort instanceof Set) {
                    return new Set();
                } else if (argumentSort instanceof Type && bodySort instanceof Type) {
                    int typeLevel = ((Type) argumentSort).level;
                    int bodyLevel = ((Type) bodySort).level;
                    return new Type(Math.max(typeLevel, bodyLevel));
                } else {
                    throw new PouletException("prod must qualify over a sort");
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

    // match _ as m(a_1, ..., a_n) in P { ... } gives us \a_1 -> ... -> \a_n -> \m -> P
    private static Expression convertMatchTypeToAbstraction(Match match, InductiveType expressionType, LocalContext context) {
        List<Symbol> argumentSymbols = new ArrayList<>(match.argumentSymbols);
        argumentSymbols.add(match.expressionSymbol);

        TypeDeclaration typeDeclaration = context.getTypeDeclaration(expressionType.inductiveType);
        ProdDecomposition prodDecomposition = new ProdDecomposition(typeDeclaration.type);
        List<Expression> argumentTypes = new ArrayList<>(prodDecomposition.argumentTypes);
        argumentTypes.add(expressionType);

        return new AbstractionDecomposition(argumentSymbols, argumentTypes, match.type).expression();
    }

    // convert clause c(a_1, ..., a_n) => e to \a_1 -> ... -> \a_n -> e
    private static Expression convertMatchBranchToAbstraction(InductiveType inductiveType, Match.Clause clause, Expression constructorDefinition, LocalContext context) {
        List<TypeDeclaration.Parameter> parameters = context.getTypeDeclaration(inductiveType.inductiveType).parameters;
        ProdDecomposition prodDecomposition = new ProdDecomposition(constructorDefinition);
        List<Expression> argumentTypes = new ArrayList<>(prodDecomposition.argumentTypes);

        for (int i = 0; i < argumentTypes.size(); i++) {
            Expression argumentType = argumentTypes.get(i);

            for (int j = 0; j < i; j++) {
                argumentType = argumentType.substitute(
                        prodDecomposition.arguments.get(j),
                        new Var(clause.argumentSymbols.get(j))
                );
            }

            for (int j = 0; j < parameters.size(); j++) {
                argumentType = argumentType.substitute(
                        parameters.get(j).name,
                        inductiveType.parameters.get(j)
                );
            }

            argumentTypes.set(i, argumentType);
        }

        return new AbstractionDecomposition(
                clause.argumentSymbols,
                argumentTypes,
                clause.expression
        ).expression();
    }

    /*private static Expression getMatchClauseType(InductiveType inductiveType, Symbol constructor, Expression returnType, LocalContext context) {
        Expression constructorDefinition = context.getConstructor(inductiveType.inductiveType, constructor).definition;
        ProdDecomposition prodDecomposition = new ProdDecomposition(constructorDefinition);
        InductiveType genericInductiveType = (InductiveType) prodDecomposition.bodyType;

        List<Expression> ccArguments = new ArrayList<>();

        for (Symbol argumentSymbol : prodDecomposition.arguments) {
            ccArguments.add(new Var(argumentSymbol));
        }

        ConstructorCall constructorCall = new ConstructorCall(
                inductiveType.inductiveType,
                inductiveType.parameters,
                constructor,
                ccArguments
        );

        List<Expression> arguments = new ArrayList<>(genericInductiveType.arguments);
        arguments.add(constructorCall);
        prodDecomposition.bodyType = new ApplicationDecomposition(
                returnType,
                arguments
        ).expression();
        return prodDecomposition.expression();
    }*/

    private static Expression getMatchClauseType(InductiveType inductiveType, Symbol constructor, Expression returnType, LocalContext context) {
        List<TypeDeclaration.Parameter> parameters = context.getTypeDeclaration(inductiveType.inductiveType).parameters;
        Expression constructorDefinition = context.getConstructor(inductiveType.inductiveType, constructor).definition;
        ProdDecomposition prodDecomposition = new ProdDecomposition(constructorDefinition);

        for (int i = 0; i < prodDecomposition.arguments.size(); i++) {
            Expression newArgumentType = prodDecomposition.argumentTypes.get(i);
            for (int j = 0; j < parameters.size(); j++) {
                Symbol symbol = parameters.get(j).name;
                Expression parameter = inductiveType.parameters.get(j);
                newArgumentType = newArgumentType.substitute(symbol, parameter);
            }
            prodDecomposition.argumentTypes.set(i, newArgumentType);
        }

        List<Expression> arguments = new ArrayList<>(((InductiveType) prodDecomposition.bodyType).arguments);

        for (int i = 0; i < arguments.size(); i++) {
            Expression newArgument = arguments.get(i);
            for (int j = 0; j < parameters.size(); j++) {
                Symbol symbol = parameters.get(j).name;
                Expression parameter = inductiveType.parameters.get(j);
                newArgument = newArgument.substitute(symbol, parameter);
            }
            arguments.set(i, newArgument);
        }

        List<Expression> argumentVars = new ArrayList<>();

        for (Symbol argumentSymbol : prodDecomposition.arguments) {
            argumentVars.add(new Var(argumentSymbol));
        }

        arguments.add(new ConstructorCall(
                inductiveType.inductiveType,
                inductiveType.parameters,
                constructor,
                argumentVars
        ));

        prodDecomposition.bodyType = new ApplicationDecomposition(
                returnType,
                arguments
        ).expression();
        return prodDecomposition.expression();
    }

    private static Expression deduceApplicationType(Application application, LocalContext context) {
        Expression functionType = deduceType(application.function, context);

        if (functionType instanceof Prod) {
            Prod prod = (Prod) functionType;
            Expression argument = Reducer.reduce(application.argument, context);
            checkType(application.argument, prod.argumentType, context);
            return Reducer.reduce(prod.bodyType.substitute(prod.argumentSymbol, argument), context);
        } else {
            throw new PouletException("can't apply to " + application.function);
        }
    }

    private static boolean isAllowedEliminationSort(InductiveType inductiveType, Expression a, Expression b, LocalContext context) {
        if (a instanceof Prod && b instanceof Prod) {
            Prod aProd = (Prod) a;
            Prod bProd = (Prod) b;

            if (Reducer.convertible(aProd.argumentType, bProd.argumentType, context)) {
                InductiveType newInductiveType = new InductiveType(inductiveType);
                newInductiveType.arguments.add(new Var());
                return isAllowedEliminationSort(newInductiveType, aProd.bodyType, bProd.bodyType, context);
            }
        } else if ((a instanceof Set || a instanceof Type) && b instanceof Prod) {
            Prod bProd = (Prod) b;
            return bProd.bodyType instanceof Sort;
        } else if (a instanceof Prop) {
            if (b instanceof Prop) {
                return true;
            }

            TypeDeclaration typeDeclaration = context.getTypeDeclaration(inductiveType.inductiveType);

            if (typeDeclaration.constructors.size() == 0) {
                return true;
            } else if (typeDeclaration.constructors.size() == 1) {
                TypeDeclaration.Constructor constructor = typeDeclaration.constructors.get(0);
                List<Expression> argumentTypes = new ProdDecomposition(constructor.definition).argumentTypes;
                return argumentTypes.stream().allMatch(argumentType -> argumentType instanceof Prop);
            }
        }

        return false;
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

    private static void checkGuarded(Fix fix) {
        List<Integer> ks = new ArrayList<>();
        List<Symbol> xs = new ArrayList<>();

        for (Fix.Clause clause : fix.clauses) {
            // make sure no recursion in types
            for (Fix.Clause otherClause : fix.clauses) {
                if (clause.type.occurs(otherClause.symbol)) {
                    throw new PouletException("no recusion allowed in types of fix clauses");
                }
            }

            AbstractionDecomposition abstractionDecomposition = new AbstractionDecomposition(clause.definition);

            if (!(abstractionDecomposition.body instanceof Match))
                throw new PouletException("body of fix definition must be a match, not a " + abstractionDecomposition.body.getClass().getSimpleName());

            Match match = (Match) abstractionDecomposition.body;

            if (!(match.expression instanceof Var))
                throw new PouletException("body of fix definition must match on an argument");

            Var var = (Var) match.expression;
            Integer k = null;

            for (int i = 0; i < abstractionDecomposition.arguments.size(); i++) {
                Symbol argument = abstractionDecomposition.arguments.get(i);

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
                checkGuarded(fix, ks, xs, i, new ArrayList<>(), clause.definition);
                return;
            }
        }

        throw new PouletException("function " + fix.mainSymbol + " not defined in " + fix);
    }

    private static void checkGuarded(Fix fix, List<Integer> ks, List<Symbol> xs, int i, List<Symbol> v, Expression expression) {
        if (expression instanceof Var) {
            Var var = (Var) expression;

            for (Fix.Clause clause : fix.clauses) {
                if (var.symbol.equals(clause.symbol)) {
                    throw new PouletException("expression " + expression + " not guarded in " + fix);
                }
            }
        } else if (expression instanceof Abstraction) {
            Abstraction abstraction = (Abstraction) expression;
            checkGuarded(fix, ks, xs, i, v, abstraction.argumentType);
            checkGuarded(fix, ks, xs, i, v, abstraction.body);
        } else if (expression instanceof Prod) {
            Prod prod = (Prod) expression;
            checkGuarded(fix, ks, xs, i, v, prod.argumentType);
            checkGuarded(fix, ks, xs, i, v, prod.bodyType);
        } else if (expression instanceof Fix) {
            Fix innerFix = (Fix) expression;
            Fix.Clause mainClause = innerFix.getMainClause();
            checkGuarded(fix, ks, xs, i, v, mainClause.type);
            checkGuarded(fix, ks, xs, i, v, mainClause.definition);
        } else if (expression instanceof InductiveType) {
            InductiveType inductiveType = (InductiveType) expression;

            for (Expression parameter : inductiveType.parameters) {
                checkGuarded(fix, ks, xs, i, v, parameter);
            }

            for (Expression argument : inductiveType.arguments) {
                checkGuarded(fix, ks, xs, i, v, argument);
            }
        } else if (expression instanceof ConstructorCall) {
            ConstructorCall constructorCall = (ConstructorCall) expression;

            for (Expression parameter : constructorCall.parameters) {
                checkGuarded(fix, ks, xs, i, v, parameter);
            }

            for (Expression argument : constructorCall.arguments) {
                checkGuarded(fix, ks, xs, i, v, argument);
            }
        } else if (expression instanceof Match) {
            Match match = (Match) expression;
            Expression matchExpression = match.expression;
            ApplicationDecomposition applicationDecomposition = new ApplicationDecomposition(matchExpression);

            if (applicationDecomposition.function instanceof Var) {
                Var var = (Var) applicationDecomposition.function;
                Symbol x = xs.get(i);

                if (var.symbol.equals(x) || v.contains(var.symbol)) {
                    checkGuarded(fix, ks, xs, i, v, match.type);

                    for (Expression argument : applicationDecomposition.arguments) {
                        checkGuarded(fix, ks, xs, i, v, argument);
                    }

                    for (Match.Clause clause : match.clauses) {
                        List<Symbol> newV = new ArrayList<>(v);
                        newV.addAll(clause.argumentSymbols);
                        checkGuarded(fix, ks, xs, i, newV, clause.expression);

                        // TODO: figure this out
                        // do we need to check whether i is a recursive position,
                        // or does that just improve efficiency?
                    }

                    return;
                }
            }

            checkGuarded(fix, ks, xs, i, v, match.type);
            checkGuarded(fix, ks, xs, i, v, match.expression);

            for (Match.Clause clause : match.clauses) {
                checkGuarded(fix, ks, xs, i, v, clause.expression);
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
                            checkGuarded(fix, ks, xs, i, v, argument);
                        }

                        if (!v.contains(call.symbol)) {
                            throw new PouletException("v doesn't contain " + call.symbol);
                        }

                        return;
                    }
                }
            }

            checkGuarded(fix, ks, xs, i, v, applicationDecomposition.function);

            for (Expression argument : applicationDecomposition.arguments) {
                checkGuarded(fix, ks, xs, i, v, argument);
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
