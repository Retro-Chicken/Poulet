package poulet.kernel;

import poulet.kernel.ast.*;
import poulet.kernel.context.LocalContext;

class Reducer {
    static Expression reduce(Expression expression, LocalContext context) {
        return expression.accept(new ExpressionVisitor<>() {
            @Override
            public Expression visit(Application application) {
                return reduceApplication(application, context);
            }

            @Override
            public Expression visit(Fix fix) {
                Fix.Clause mainClause = fix.getMainClause();
                return reduce(mainClause.definition, context);
            }

            @Override
            public Expression visit(Match match) {
                Expression matchExpression = reduce(match.expression, context);

                return matchExpression.accept(new ExpressionVisitor<>() {
                    @Override
                    public Expression visit(ConstructorCall constructorCall) {
                        Match.Clause matchingClause = match.getClause(constructorCall.constructor);
                        LocalContext clauseContext = new LocalContext(context);

                        for (int i = 0; i < matchingClause.argumentSymbols.size(); i++) {
                            Symbol symbol = matchingClause.argumentSymbols.get(i);
                            Expression argument = constructorCall.arguments.get(i);
                            context.define(symbol, argument);
                        }

                        return reduce(matchingClause.expression, clauseContext);
                    }
                });
            }

            @Override
            public Expression visit(Var var) {
                // delta reduction
                Expression definition = context.getDefinition(var.symbol);

                if (definition == null) {
                    return var;
                } else {
                    return definition;
                }
            }

            @Override
            public Expression other(Expression expression) {
                return expression;
            }
        });
    }

    private static Expression reduceApplication(Application application, LocalContext context) {
        return application.function.accept(new ExpressionVisitor<>() {
            @Override
            public Expression visit(Abstraction abstraction) {
                // beta reduction
                LocalContext newContext = new LocalContext(context);
                newContext.define(
                        abstraction.argumentSymbol,
                        application.argument
                );
                return reduce(abstraction.body, newContext);
            }

            @Override
            public Expression visit(ConstructorCall constructorCall) {
                // instantiate constructor arguments
                Expression argument = reduce(application.argument, context);
                ConstructorCall newConstructorCall = new ConstructorCall(constructorCall);
                newConstructorCall.arguments.add(argument);
                return newConstructorCall;
            }

            @Override
            public Expression visit(InductiveType inductiveType) {
                // instantiate inductive type arguments (only valid inside constructor definitions)
                Expression argument = reduce(application.argument, context);
                InductiveType newInductiveType = new InductiveType(inductiveType);
                newInductiveType.arguments.add(argument);
                return newInductiveType;
            }

            @Override
            public Expression other(Expression expression) {
                return new Application(
                        reduce(application.function, context),
                        application.argument
                );
            }
        });
    }

    static boolean convertible(Expression a, Expression b, LocalContext context) {
        Expression aReduced = reduce(a.normalizeUniqueSymbols(), context);
        Expression bReduced = reduce(b.normalizeUniqueSymbols(), context);

        return alphaConvertible(aReduced, bReduced) ||
                etaConvertible(aReduced, bReduced, context);
    }

    private static boolean alphaConvertible(Expression a, Expression b) {
        // symbols already normalized, so equality equivalent to alpha convertibility
        return a.equals(b);
    }

    private static boolean etaConvertible(Expression a, Expression b, LocalContext context) {
        if (etaConvertibleDirectional(a, b, context)) {
            return true;
        } else {
            return etaConvertibleDirectional(b, a, context);
        }
    }

    private static boolean etaConvertibleDirectional(Expression a, Expression b, LocalContext context) {
        return a.accept(new ExpressionVisitor<>() {
            @Override
            public Boolean visit(Abstraction abstraction) {
                Application application = new Application(
                        b,
                        new Var(abstraction.argumentSymbol)
                );
                return convertible(abstraction.body, application, context);
            }

            @Override
            public Boolean other(Expression expression) {
                return false;
            }
        });
    }
}
