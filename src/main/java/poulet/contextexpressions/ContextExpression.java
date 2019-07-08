package poulet.contextexpressions;

import poulet.ast.Expression;
import poulet.typing.Environment;

public abstract class ContextExpression {
    public final Expression expression;
    public final Environment environment;

    protected ContextExpression(Expression expression, Environment environment) {
        this.expression = expression;
        this.environment = environment;
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
}
