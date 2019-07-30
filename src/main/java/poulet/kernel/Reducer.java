package poulet.kernel;

import poulet.PouletException;
import poulet.kernel.ast.*;
import poulet.kernel.context.LocalContext;
import poulet.kernel.decomposition.AbstractionDecomposition;
import poulet.kernel.decomposition.ApplicationDecomposition;

import java.util.ArrayList;
import java.util.List;

class Reducer {
    static Expression reduce(Expression expression, LocalContext context) {
        //List<Expression> steps = new ArrayList<>();
        Expression last = expression;
        //steps.add(last);

        while (true) {
            Expression next = reduceStep(last, context);

            if (next.equals(last)) {
                // no more reduction possible, normal form reached
                /*System.out.println("BEGIN REDUCTION");

                for (int i = 0; i < steps.size(); i++) {
                    System.out.println("" + i + ". " + steps.get(i).context);
                    System.out.println("" + i + ". " + steps.get(i).expression);
                }

                System.out.println("END REDUCTION");*/

                return reduceAbstractionTypesAndApplicationArguments(last, context);
            }

            //steps.add(next);
            last = next;
        }
    }

    private static Expression reduceAbstractionTypesAndApplicationArguments(Expression expression, LocalContext context) {
        AbstractionDecomposition abstractionDecomposition = new AbstractionDecomposition(expression);
        ApplicationDecomposition applicationDecomposition = new ApplicationDecomposition(abstractionDecomposition.body);

        for (int i = 0;i < abstractionDecomposition.argumentTypes.size(); i++) {
            abstractionDecomposition.argumentTypes.set(i, reduce(abstractionDecomposition.argumentTypes.get(i), context));
        }

        for (int i = 0;i < applicationDecomposition.arguments.size(); i++) {
            applicationDecomposition.arguments.set(i, reduce(applicationDecomposition.arguments.get(i), context));
        }

        abstractionDecomposition.body = applicationDecomposition.expression();
        return abstractionDecomposition.expression();
    }

    private static Expression reduceStep(Expression expression, LocalContext context) {
        AbstractionDecomposition abstractionDecomposition = new AbstractionDecomposition(expression);
        ApplicationDecomposition applicationDecomposition = new ApplicationDecomposition(abstractionDecomposition.body);
        Expression head = applicationDecomposition.function;

        return head.accept(new ExpressionVisitor<>() {
            @Override
            public Expression visit(Abstraction abstraction) {
                applicationDecomposition.function = abstraction.body.substitute(
                        abstraction.argumentSymbol,
                        applicationDecomposition.arguments.remove(0)
                );
                abstractionDecomposition.body = applicationDecomposition.expression();
                return abstractionDecomposition.expression();
            }

            @Override
            public Expression visit(ConstructorCall constructorCall) {
                if (applicationDecomposition.arguments.size() == 0) {
                    return expression;
                } else {
                    // instantiate constructor arguments
                    Expression argument = applicationDecomposition.arguments.remove(0);
                    Expression reducedArgument = reduce(argument, context);
                    ConstructorCall newConstructorCall = new ConstructorCall(constructorCall);
                    newConstructorCall.arguments.add(reducedArgument);

                    applicationDecomposition.function = newConstructorCall;
                    abstractionDecomposition.body = applicationDecomposition.expression();
                    return abstractionDecomposition.expression();
                }
            }

            @Override
            public Expression visit(InductiveType inductiveType) {
                if (applicationDecomposition.arguments.size() == 0) {
                    return expression;
                } else {
                    // instantiate inductive type arguments
                    Expression argument = applicationDecomposition.arguments.remove(0);
                    Expression reducedArgument = reduce(argument, context);
                    InductiveType newInductiveType = new InductiveType(inductiveType);
                    newInductiveType.arguments.add(reducedArgument);

                    applicationDecomposition.function = newInductiveType;
                    abstractionDecomposition.body = applicationDecomposition.expression();
                    return abstractionDecomposition.expression();
                }
            }

            @Override
            public Expression visit(Fix fix) {
                Expression mainClause = fix.getMainClause().definition;

                // Make sure we've passed the decreasing argument before reducing
                int k = fix.ks.get(fix.getClauseIndex(fix.mainSymbol));

                if(applicationDecomposition.arguments.size() - 1 < k)
                    return applicationDecomposition.expression();
                if(!(reduce(applicationDecomposition.arguments.get(k), context) instanceof ConstructorCall))
                    return applicationDecomposition.expression();

                for (Fix.Clause clause : fix.clauses) {
                    mainClause.substitute(
                            clause.symbol,
                            new Fix(fix.clauses, clause.symbol, fix.ks, fix.xs)
                    );
                }

                applicationDecomposition.function = mainClause;
                abstractionDecomposition.body = applicationDecomposition.expression();
                return abstractionDecomposition.expression();
            }

            @Override
            public Expression visit(Match match) {
                Expression matchExpression = reduce(match.expression, context);

                Expression reducedMatch = matchExpression.accept(new ExpressionVisitor<>() {
                    @Override
                    public Expression visit(ConstructorCall constructorCall) {
                        Match.Clause matchingClause = match.getClause(constructorCall.constructor);
                        Expression matchingExpression = matchingClause.expression;

                        for (int i = 0; i < matchingClause.argumentSymbols.size(); i++) {
                            Symbol symbol = matchingClause.argumentSymbols.get(i);
                            Expression argument = constructorCall.arguments.get(i);
                            matchingExpression = matchingExpression.substitute(symbol, argument);
                        }

                        applicationDecomposition.function = matchingExpression;
                        abstractionDecomposition.body = applicationDecomposition.expression();
                        return abstractionDecomposition.expression();
                    }

                    @Override
                    public Expression other(Expression expression) {
                        List<Match.Clause> clauses = new ArrayList<>();

                        for (Match.Clause clause : match.clauses) {
                            clauses.add(new Match.Clause(
                                    clause.constructor,
                                    clause.argumentSymbols,
                                    reduce(clause.expression, context)
                            ));
                        }

                        return new Match(
                                matchExpression,
                                match.expressionSymbol,
                                match.argumentSymbols,
                                reduce(match.type, context),
                                clauses
                        );
                    }
                });


                applicationDecomposition.function = reducedMatch;
                abstractionDecomposition.body = applicationDecomposition.expression();
                return abstractionDecomposition.expression();
            }

            @Override
            public Expression visit(Prod prod) {
                applicationDecomposition.function = new Prod(
                        prod.argumentSymbol,
                        reduce(prod.argumentType, context),
                        reduceStep(prod.bodyType, context)
                );
                abstractionDecomposition.body = applicationDecomposition.expression();
                return abstractionDecomposition.expression();
            }

            @Override
            public Expression visit(Var var) {
                // delta reduction
                Expression definition = context.getDefinition(var.symbol);

                if (definition == null) {
                    return expression;
                } else {
                    applicationDecomposition.function = definition;
                    abstractionDecomposition.body = applicationDecomposition.expression();
                    return abstractionDecomposition.expression();
                }
            }

            @Override
            public Expression other(Expression expression) {
                return expression;
            }
        });
    }

    static boolean convertible(Expression a, Expression b, LocalContext context) {
        Expression aReduced = reduce(a, context);
        Expression bReduced = reduce(b, context);
        return alphaConvertible(aReduced, bReduced) || etaConverible(aReduced, bReduced, context);
    }

    private static boolean alphaConvertible(Expression a, Expression b) {
        return a.normalizeUniqueSymbols().equals(b.normalizeUniqueSymbols());
    }

    private static boolean etaConverible(Expression a, Expression b, LocalContext context) {
        return etaConvertibleDirectional(a, b, context) || etaConvertibleDirectional(b, a, context);
    }

    private static boolean etaConvertibleDirectional(Expression a, Expression b, LocalContext context) {
        if (a instanceof Abstraction) {
            Abstraction abstraction = (Abstraction) a;
            Application application = new Application(
                b,
                new Var(abstraction.argumentSymbol)
            );
            return convertible(abstraction.body, application, context);
        }

        return false;
    }
}
