package poulet.interpreter;

import poulet.ast.*;
import poulet.exceptions.PouletException;
import poulet.inference.Inferer;
import poulet.typing.Environment;
import poulet.util.ExpressionVisitor;

import java.util.ArrayList;
import java.util.List;

public class Evaluator {
    public static Expression reduce(Expression expression, Environment environment) throws PouletException {
        return reduce(expression.context(environment));
    }

    public static Expression reduce(Expression expression) throws PouletException {
        return expression.accept(new ExpressionVisitor<>() {
            @Override
            public Expression visit(Application application) throws PouletException {
                application = Inferer.fillImplicitArguments(application);

                Expression function = reduce(application.function);
                Expression argument = reduce(application.argument);

                return function.accept(new ExpressionVisitor<>() {
                    @Override
                    public Expression visit(Abstraction abstraction) throws PouletException {
                        // TODO: figure out if this makes sense
                        // do we need to store an Environment with everything in scope?
                        return reduce(abstraction.body.context(abstraction.body.environment.appendScope(abstraction.symbol, argument)));
                    }

                    @Override
                    public Expression visit(ConstructorCall constructorCall) throws PouletException {
                        ArrayList<Expression> newArguments = new ArrayList<>(constructorCall.arguments);
                        newArguments.add(argument);

                        return new ConstructorCall(
                                        constructorCall.inductiveType,
                                        constructorCall.constructor,
                                        newArguments,
                                        constructorCall.environment
                                );
                    }

                    @Override
                    public Expression visit(InductiveType inductiveType) throws PouletException {
                        ArrayList<Expression> newArguments = new ArrayList<>(inductiveType.arguments);
                        newArguments.add(argument);

                        return new InductiveType(
                                        inductiveType.type,
                                        inductiveType.isConcrete(),
                                        inductiveType.parameters,
                                        newArguments,
                                        inductiveType.environment
                                );
                    }

                    @Override
                    public Expression other(Expression expression) throws PouletException {
                        return new Application(function, argument, function.environment);
                    }
                });
            }

            @Override
            public Expression visit(ConstructorCall constructorCall) throws PouletException {
                List<Expression> arguments = new ArrayList<>();

                if (constructorCall.isConcrete()) {
                    for (Expression argument : constructorCall.arguments) {
                        arguments.add(reduce(argument));
                    }
                }

                return new ConstructorCall(
                            constructorCall.inductiveType,
                            constructorCall.constructor,
                            arguments,
                            constructorCall.environment
                        );
            }

            @Override
            public Expression visit(Fix fix) throws PouletException {
                Definition exported = fix.getExported();
                return reduce(exported.definition);
            }

            @Override
            public Expression visit(InductiveType inductiveType) throws PouletException {
                if (inductiveType.isConcrete()) {
                    List<Expression> parameters = new ArrayList<>();
                    for (Expression parameter : inductiveType.parameters) {
                        parameters.add(reduce(parameter));
                    }

                    List<Expression> arguments = new ArrayList<>();
                    for (Expression argument : inductiveType.arguments) {
                        arguments.add(reduce(argument));
                    }

                    return new InductiveType(
                                    inductiveType.type,
                                    true,
                                    parameters,
                                    arguments,
                                    inductiveType.environment
                            );
                } else {
                    return new InductiveType(
                                    inductiveType.type,
                                    true,
                                    inductiveType.parameters,
                                    new ArrayList<>(),
                                    inductiveType.environment
                            );
                }
            }

            @Override
            public Expression visit(Match match) throws PouletException {
                Expression matchExpression = reduce(match.expression);

                return matchExpression.accept(new ExpressionVisitor<>() {
                    @Override
                    public Expression visit(ConstructorCall constructorCall) throws PouletException {
                        if (!constructorCall.isConcrete()) {
                            throw new PouletException("can't match on non-concrete constructor call");
                        }

                        Match.Clause matchingClause = match.getClause(constructorCall.constructor);
                        Environment newEnvironment = expression.environment;

                        for (int i = 0; i < matchingClause.argumentSymbols.size(); i++) {
                            Symbol symbol = matchingClause.argumentSymbols.get(i);
                            Expression argument = constructorCall.arguments.get(i);
                            newEnvironment = newEnvironment.appendScope(symbol, argument);
                        }

                        return reduce(matchingClause.expression.context(newEnvironment));
                    }
                });
            }

            @Override
            public Expression visit(PiType piType) throws PouletException {
                return new PiType(
                                piType.variable,
                                reduce(piType.type),
                                reduce(piType.body),
                                piType.inferable,
                                piType.environment
                        );
            }

            @Override
            public Expression visit(Variable variable) throws PouletException {
                Expression value = expression.environment.lookUpScope(variable.symbol);

                if (value == null) {
                    return expression;
                } else {
                    return reduce(value, expression.environment);
                }
            }

            @Override
            public Expression other(Expression expression) {
                return expression;
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
                        new Variable(abstraction.symbol, environment),
                        environment
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