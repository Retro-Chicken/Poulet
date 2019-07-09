package poulet.interpreter;

import poulet.ast.*;
import poulet.contextexpressions.*;
import poulet.exceptions.PouletException;
import poulet.inference.Inferer;
import poulet.typing.Checker;
import poulet.typing.Environment;
import poulet.util.ContextExpressionVisitor;
import poulet.util.ExpressionVisitor;

import java.util.ArrayList;
import java.util.List;

public class Evaluator {
    public static Expression reduce(Expression expression, Environment environment) throws PouletException {
        return reduce(expression.contextExpression(environment)).expression;
    }

    public static ContextExpression reduce(ContextExpression reducable) throws PouletException {
        return reducable.accept(new ContextExpressionVisitor<>() {
            @Override
            public ContextExpression visit(ContextApplication application) throws PouletException {
                application = Inferer.fillImplicitArguments(application);

                ContextExpression function = reduce(application.function);
                ContextExpression argument = reduce(application.argument);

                return function.accept(new ContextExpressionVisitor<>() {
                    @Override
                    public ContextExpression visit(ContextAbstraction abstraction) throws PouletException {
                        // TODO: figure out if this makes sense
                        // do we need to store an Environment with everything in scope?

                        ContextAbstraction result = abstraction;
                        // ------------------------- INFERENCE  ----------------------------
                        /*
                        List<Expression> implictArguments = Inferer.getImplicitArguments(application);
                        for(Expression implicitArgument : implictArguments) {
                            result = (ContextAbstraction) result.body.appendScope(result.symbol, implicitArgument);
                        }*/
                        // -----------------------------------------------------------------
                        return reduce(result.body.appendScope(abstraction.symbol, argument.expression));
                    }

                    @Override
                    public ContextExpression visit(ContextConstructorCall constructorCall) throws PouletException {
                        ArrayList<ContextExpression> newArguments = new ArrayList<>(constructorCall.arguments);
                        newArguments.add(argument);

                        return new ContextConstructorCall(
                                        constructorCall.inductiveType,
                                        constructorCall.constructor,
                                        newArguments
                                );
                    }

                    @Override
                    public ContextExpression visit(ContextInductiveType inductiveType) throws PouletException {
                        ArrayList<ContextExpression> newArguments = new ArrayList<>(inductiveType.arguments);
                        newArguments.add(argument);

                        return new ContextInductiveType(
                                        inductiveType.type,
                                        inductiveType.isConcrete(),
                                        inductiveType.parameters,
                                        newArguments,
                                        inductiveType.environment
                                );
                    }

                    @Override
                    public ContextExpression other(ContextExpression expression) throws PouletException {
                        return new ContextApplication(function, argument);
                    }
                });
            }

            @Override
            public ContextExpression visit(ContextConstructorCall constructorCall) throws PouletException {
                List<ContextExpression> arguments = new ArrayList<>();

                if (constructorCall.isConcrete()) {
                    for (ContextExpression argument : constructorCall.arguments) {
                        arguments.add(reduce(argument));
                    }
                }

                return new ContextConstructorCall(
                            constructorCall.inductiveType,
                            constructorCall.constructor,
                            arguments
                        );
            }

            @Override
            public ContextExpression visit(ContextFix fix) throws PouletException {
                ContextDefinition exported = fix.getExported();
                return reduce(exported.definition);
            }

            @Override
            public ContextExpression visit(ContextInductiveType inductiveType) throws PouletException {
                if (inductiveType.isConcrete()) {
                    List<ContextExpression> parameters = new ArrayList<>();
                    for (ContextExpression parameter : inductiveType.parameters) {
                        parameters.add(reduce(parameter));
                    }

                    List<ContextExpression> arguments = new ArrayList<>();
                    for (ContextExpression argument : inductiveType.arguments) {
                        arguments.add(reduce(argument));
                    }

                    return new ContextInductiveType(
                                    inductiveType.type,
                                    true,
                                    parameters,
                                    arguments,
                                    inductiveType.environment
                            );
                } else {
                    return new ContextInductiveType(
                                    inductiveType.type,
                                    true,
                                    inductiveType.parameters,
                                    new ArrayList<>(),
                                    inductiveType.environment
                            );
                }
            }

            @Override
            public ContextExpression visit(ContextMatch match) throws PouletException {
                ContextExpression matchExpression = reduce(match.expression);

                return matchExpression.expression.accept(new ExpressionVisitor<>() {
                    @Override
                    public ContextExpression visit(ConstructorCall constructorCall) throws PouletException {
                        if (!constructorCall.isConcrete()) {
                            throw new PouletException("can't match on non-concrete constructor call");
                        }

                        ContextMatch.Clause matchingClause = match.getClause(constructorCall.constructor);
                        Environment newEnvironment = reducable.environment;

                        for (int i = 0; i < matchingClause.argumentSymbols.size(); i++) {
                            Symbol symbol = matchingClause.argumentSymbols.get(i);
                            Expression argument = constructorCall.arguments.get(i);
                            newEnvironment = newEnvironment.appendScope(symbol, argument);
                        }

                        return reduce(matchingClause.expression.expression.contextExpression(newEnvironment));
                    }
                });
            }

            @Override
            public ContextExpression visit(ContextPiType piType) throws PouletException {
                return new ContextPiType(
                                piType.variable,
                                reduce(piType.type),
                                reduce(piType.body),
                                piType.inferable
                        );
            }

            @Override
            public ContextExpression visit(ContextVariable variable) throws PouletException {
                Expression value = reducable.environment.lookUpScope(variable.symbol);

                if (value == null) {
                    return reducable;
                } else {
                    return reduce(value.contextExpression(reducable.environment));
                }
            }

            @Override
            public ContextExpression other(ContextExpression expression) {
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