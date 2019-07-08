package poulet.contextexpressions;

import poulet.ast.Expression;
import poulet.ast.Symbol;
import poulet.exceptions.PouletException;
import poulet.typing.Environment;
import poulet.util.ContextExpressionVisitor;

public abstract class ContextExpression {
    public final Expression expression;
    public final Environment environment;

    protected ContextExpression(Expression expression, Environment environment) {
        this.expression = expression;
        this.environment = environment;
    }

    public ContextExpression appendScope(Symbol name, Expression value) throws PouletException {
        return expression.contextExpression(environment.appendScope(name, value));
    }

    public ContextExpression appendType(Symbol name, Expression value) throws PouletException {
        return expression.contextExpression(environment.appendType(name, value));
    }

    @Override
    public String toString() {
        return expression.toString() + "\nIn Environment\n" + environment.toString();
    }

    @Override
    public boolean equals(Object object) {
        if(object instanceof ContextExpression) {
            ContextExpression other = (ContextExpression) object;
            return expression.equals(other.expression) && environment.equals(other.environment);
        }
        return false;
    }

    public abstract <T> T accept(ContextExpressionVisitor<T> visitor) throws PouletException;
}
