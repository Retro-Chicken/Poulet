package poulet.interpreter;

import poulet.ast.*;
import poulet.exceptions.PouletException;
import poulet.typing.Environment;
import poulet.util.ExpressionVisitor;

import java.util.ArrayList;
import java.util.List;

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

                    @Override
                    public Reducable visit(InductiveType inductiveType) throws PouletException {
                        ArrayList<Expression> newArguments = new ArrayList<>(inductiveType.arguments);
                        newArguments.add(reduce(argument, environment));

                        return new Reducable(
                                new InductiveType(
                                        inductiveType.type,
                                        inductiveType.isConcrete(),
                                        inductiveType.parameters,
                                        newArguments
                                ),
                                environment
                        );
                    }

                    @Override
                    public Reducable other(Expression expression) {
                        return new Reducable(
                                new Application(function.expression, argument),
                                environment
                        );
                    }
                });
            }

            @Override
            public Reducable visit(ConstructorCall constructorCall) throws PouletException {
                List<Expression> arguments = new ArrayList<>();

                if (constructorCall.isConcrete()) {
                    for (Expression argument : constructorCall.arguments) {
                        arguments.add(reduce(argument, environment));
                    }
                }

                return new Reducable(
                        new ConstructorCall(
                                constructorCall.inductiveType,
                                constructorCall.constructor,
                                arguments
                        ),
                        environment
                );
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
            public Reducable visit(InductiveType inductiveType) throws PouletException {
                if (inductiveType.isConcrete()) {
                    List<Expression> parameters = new ArrayList<>();
                    for (Expression parameter : inductiveType.parameters) {
                        parameters.add(reduce(parameter, environment));
                    }

                    List<Expression> arguments = new ArrayList<>();
                    for (Expression argument : inductiveType.arguments) {
                        arguments.add(reduce(argument, environment));
                    }

                    return new Reducable(
                            new InductiveType(
                                    inductiveType.type,
                                    true,
                                    parameters,
                                    arguments
                            ),
                            environment
                    );
                } else {
                    return new Reducable(
                            new InductiveType(
                                    inductiveType.type,
                                    true,
                                    inductiveType.parameters,
                                    new ArrayList<>()
                            ),
                            environment
                    );
                }
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
            public Reducable visit(PiType piType) throws PouletException {
                return new Reducable(
                        new PiType(
                                piType.variable,
                                reduce(piType.type, environment),
                                reduce(piType.body, environment)
                        ),
                        environment
                );
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

    private static boolean etaConvertible(Expression a, Expression b, Environment environment) throws
            PouletException {
        if (etaConvertibleDirectional(a, b, environment)) {
            return true;
        } else {
            return etaConvertibleDirectional(b, a, environment);
        }
    }

    private static boolean etaConvertibleDirectional(Expression a, Expression b, Environment environment) throws
            PouletException {
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