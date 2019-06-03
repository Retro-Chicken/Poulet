package poulet.interpreter;

import poulet.ast.*;
import poulet.exceptions.PouletException;
import poulet.typing.Environment;
import poulet.util.ExpressionVisitor;

import java.util.ArrayList;

public class Evaluator {
    private static class Reducable {
        private Expression expression;
        private Environment environment;

        private Reducable(Expression expression, Environment environment) {
            this.expression = expression;
            this.environment = environment;
        }
    }

    public static Expression reduce(Expression expression, Environment environment) throws PouletException {
        return reduce(new Reducable(expression, environment)).expression;
    }

    private static Reducable reduce(Reducable reducable) throws PouletException {
        Expression expression = reducable.expression;
        Environment environment = reducable.environment;

        return expression.accept(new ExpressionVisitor<>() {
            @Override
            public Reducable visit(Application application) throws PouletException {
                Reducable function = reduce(new Reducable(application.function, environment));
                Expression argument = reduce(new Reducable(application.argument, environment)).expression;

                return function.expression.accept(new ExpressionVisitor<>() {
                    @Override
                    public Reducable visit(Abstraction abstraction) throws PouletException {
                        // TODO: figure out if this makes sense
                        // do we need to store an Environment with everything in scope?
                        return reduce(new Reducable(
                                abstraction.body,
                                function.environment.appendScope(abstraction.symbol, argument)
                        ));
                    }

                    @Override
                    public Reducable visit(ConstructorCall constructorCall) {
                        ArrayList<Expression> newArguments = new ArrayList<>(constructorCall.arguments);
                        newArguments.add(argument);

                        return new Reducable(
                                new ConstructorCall(
                                        constructorCall.inductiveType,
                                        constructorCall.constructor,
                                        newArguments
                                ),
                                environment
                        );
                    }
                });
            }

            @Override
            public Reducable visit(ConstructorCall constructorCall) {
                if (constructorCall.isConcrete()) {
                    return reducable;
                } else {
                    return new Reducable(
                            new ConstructorCall(
                                    constructorCall.inductiveType,
                                    constructorCall.constructor,
                                    new ArrayList<>()
                            ),
                            environment
                    );
                }
            }

            @Override
            public Reducable visit(Fix fix) throws PouletException {
                Definition exported = fix.getExported();
                Environment newEnvironment = environment;

                for (Definition definition : fix.definitions) {
                    Fix newFix = new Fix(
                            fix.definitions,
                            definition.name
                    );
                    newEnvironment = newEnvironment.appendScope(
                            definition.name,
                            newFix
                    );
                }

                return reduce(new Reducable(
                        exported.definition,
                        newEnvironment
                ));
            }

            @Override
            public Reducable visit(Match match) throws PouletException {
                Reducable matchExpression = reduce(new Reducable(
                        match.expression,
                        environment
                ));

                return matchExpression.expression.accept(new ExpressionVisitor<>() {
                    @Override
                    public Reducable visit(ConstructorCall constructorCall) throws PouletException {
                        if (!constructorCall.isConcrete()) {
                            throw new PouletException("can't match on non-concrete constructor call");
                        }

                        Match.Clause matchingClause = match.getClause(constructorCall.constructor);
                        Environment newEnvironment = environment;

                        for (int i = 0; i < matchingClause.argumentSymbols.size(); i++) {
                            Symbol symbol = matchingClause.argumentSymbols.get(i);
                            Expression argument = constructorCall.arguments.get(i);
                            newEnvironment = newEnvironment.appendScope(symbol, argument);
                        }

                        return reduce(new Reducable(
                                matchingClause.expression,
                                newEnvironment
                        ));
                    }
                });
            }

            @Override
            public Reducable visit(Variable variable) throws PouletException {
                Expression value = reducable.environment.lookUpScope(variable.symbol);

                if (value == null) {
                    return reducable;
                } else {
                    return reduce(new Reducable(
                            value,
                            reducable.environment
                    ));
                }
            }

            @Override
            public Reducable other(Expression expression) {
                return reducable;
            }
        });
    }

    public static boolean convertible(Expression a, Expression b, Environment environment) throws PouletException {
        Expression aReduced = reduce(a, environment);
        Expression bReduced = reduce(b, environment);

        return alphaConvertible(aReduced, bReduced) ||
                etaConvertible(aReduced, bReduced, environment);
    }

    private static boolean alphaConvertible(Expression a, Expression b) throws PouletException {
        Expression aUnique = a.normalizeSymbolNames();
        Expression bUnique = b.normalizeSymbolNames();
        return aUnique.toString().equals(bUnique.toString());
    }

    private static boolean etaConvertible(Expression a, Expression b, Environment environment) throws PouletException {
        if (etaConvertibleDirectional(a, b, environment)) {
            return true;
        } else {
            return etaConvertibleDirectional(b, a, environment);
        }
    }

    private static boolean etaConvertibleDirectional(Expression a, Expression b, Environment environment) throws PouletException {
        return a.accept(new ExpressionVisitor<>() {
            @Override
            public Boolean visit(Abstraction abstraction) throws PouletException {
                Application application = new Application(
                        b,
                        new Variable(abstraction.symbol)
                );
                return convertible(abstraction.body, application, environment);
            }

            @Override
            public Boolean other(Expression expression) {
                return false;
            }
        });
    }
}