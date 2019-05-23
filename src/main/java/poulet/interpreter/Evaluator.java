package poulet.interpreter;

import poulet.ast.*;
import poulet.exceptions.PouletException;
import poulet.typing.Environment;

import java.util.ArrayList;
import java.util.List;

public class Evaluator {
    public static Expression reduce(Expression expression, Environment environment) throws PouletException {
        return expression.accept(new ExpressionVisitor<Expression>() {
            @Override
            public Expression visit(Application application) throws PouletException {
                return beta_reduce(application, environment);
            }

            @Override
            public Expression visit(Variable variable) throws PouletException {
                return delta_reduce(variable, environment);
            }

            @Override
            public Expression visit(Match match) throws PouletException {
                return iota_reduce(match, environment);
            }

            @Override
            public Expression other(Expression expression) {
                return expression;
            }
        });
    }

    private static Expression beta_reduce(Application application, Environment environment) throws PouletException {
        ApplicationDecomposition applicationDecomposition = getApplicationDecomposition(application);

        return applicationDecomposition.function.accept(new ExpressionVisitor<Expression>() {
            @Override
            public Expression visit(Abstraction abstraction) throws PouletException {
                return abstraction.body.substitute(abstraction.symbol, application.argument);
            }

            @Override
            public Expression visit(ConstructorCall constructorCall) throws PouletException {
                if (constructorCall.isConcrete()) {
                    throw new PouletException("can't apply to a concrete ConstructorCall");
                }

                Constructor constructor = environment.lookUpConstructor(constructorCall);
                if (constructor == null) {
                    throw new PouletException("constructor for " + constructorCall + " doesn't exist");
                }

                List<Expression> arguments = new ArrayList<>();
                for (Expression argument : applicationDecomposition.arguments) {
                    arguments.add(reduce(argument, environment));
                }

                return new ConstructorCall(
                        (InductiveType) reduce(constructorCall.inductiveType, environment),
                        constructorCall.constructor,
                        arguments
                );
            }

            @Override
            public Expression other(Expression expression) {
                return expression;
            }
        });
    }

    private static Expression delta_reduce(Variable variable, Environment environment) throws PouletException {
        Expression value = environment.lookUpScope(variable.symbol);

        if (value == null) {
            if (variable.isFree()) {
                return variable;
            } else {
                throw new PouletException("undefined reference to non-free variable " + variable);
            }
        } else {
            return value;
        }
    }

    private static Expression iota_reduce(Match match, Environment environment) throws PouletException {
        Expression expressionReduced = reduce(match.expression, environment);
        return expressionReduced.accept(new ExpressionVisitor<Expression>() {
            @Override
            public Expression visit(ConstructorCall constructorCall) throws PouletException {
                if (!constructorCall.isConcrete()) {
                    throw new PouletException("can't match on non-concrete constructor call");
                }

                for (Match.Clause clause : match.clauses) {
                    if (clause.constructorSymbol.equals(constructorCall.constructor)) {
                        Environment newEnvironment = environment.copy();

                        if (constructorCall.arguments.size() != clause.argumentSymbols.size()) {
                            throw new PouletException("wrong number of arguments for constructor " + constructorCall.constructor + " in match " + match);
                        }

                        for (int i = 0; i < clause.argumentSymbols.size(); i++) {
                            newEnvironment.appendScope(
                                    clause.argumentSymbols.get(i),
                                    constructorCall.arguments.get(i)
                            );
                        }

                        return reduce(clause.expression, newEnvironment);
                    }
                }

                throw new PouletException("no branch for constructor " + constructorCall.constructor + " in match " + match);
            }

            @Override
            public Expression other(Expression expression) throws PouletException {
                return expression;
            }
        });
    }

    public static boolean convertible(Expression a, Expression b, Environment environment) throws PouletException {
        Expression aReduced = reduce(a, environment);
        Expression bReduced = reduce(b, environment);

        return alpha_convertible(aReduced, bReduced) ||
                eta_convertible(aReduced, bReduced, environment);
    }

    private static boolean alpha_convertible(Expression a, Expression b) throws PouletException {
        Expression aUnique = a.normalizeSymbolNames();
        Expression bUnique = b.normalizeSymbolNames();
        return aUnique.toString().equals(bUnique.toString());
    }

    private static boolean eta_convertible(Expression a, Expression b, Environment environment) throws PouletException {
        if (eta_convertible_directional(a, b, environment)) {
            return true;
        } else {
            return eta_convertible_directional(b, a, environment);
        }
    }

    private static boolean eta_convertible_directional(Expression a, Expression b, Environment environment) throws PouletException {
        return a.accept(new ExpressionVisitor<Boolean>() {
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

    private static class ApplicationDecomposition {
        private Expression function = null;
        private List<Expression> arguments = new ArrayList<>();
    }

    // decomposes into M = f(a_1, ..., a_n) with f not being an application
    private static ApplicationDecomposition getApplicationDecomposition(Expression expression) {
        return getApplicationDecomposition(expression, new ApplicationDecomposition());
    }

    private static ApplicationDecomposition getApplicationDecomposition(Expression expression, ApplicationDecomposition applicationDecomposition) {
        if (expression instanceof Application) {
            Application application = (Application) expression;
            applicationDecomposition.arguments.add(0, application.argument);
            return getApplicationDecomposition(application.function, applicationDecomposition);
        } else {
            applicationDecomposition.function = expression;
            return applicationDecomposition;
        }
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

    // decomposes into M = {a_1 : A_1} ... {a_n : A_n} N with N not being a pi type
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
}
