package poulet.interpreter;

import poulet.ast.*;
import poulet.exceptions.PouletException;
import poulet.typing.Environment;

public class Evaluator {
    public static Expression reduce(Expression expression, Environment environment) throws PouletException {
        return expression.accept(new ExpressionVisitor<Expression>() {
            @Override
            public Expression visit(Application application) throws PouletException {
                return beta_reduce(application);
            }

            @Override
            public Expression visit(Variable variable) throws PouletException {
                return delta_reduce(variable, environment);
            }

            @Override
            public Expression other(Expression expression) {
                return expression;
            }
        });
    }

    private static Expression beta_reduce(Application application) throws PouletException {
        return application.function.accept(new ExpressionVisitor<Expression>() {
            @Override
            public Expression visit(Abstraction abstraction) throws PouletException {
                return abstraction.body.substitute(abstraction.symbol, application.argument);
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

    public static boolean convertible(Expression a, Expression b, Environment environment) throws PouletException {
        Expression aReduced = reduce(a, environment);
        Expression bReduced = reduce(b, environment);

        if (identical(aReduced, bReduced))
            return true;

        return eta_convertible(a, b, environment);
    }

    private static boolean identical(Expression a, Expression b) throws PouletException {
        int oldNextId = Symbol.nextId;
        Symbol.nextId = 0;
        Expression aUnique = a.makeSymbolsUnique();
        Symbol.nextId = 0;
        Expression bUnique = b.makeSymbolsUnique();
        Symbol.nextId = oldNextId;
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
}
