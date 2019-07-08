package poulet.contextexpressions;

import poulet.ast.Type;
import poulet.exceptions.PouletException;
import poulet.typing.Environment;
import poulet.util.ContextExpressionVisitor;

public class ContextType extends ContextExpression {
    public ContextType(Type type, Environment environment) {
        super(type, environment);
    }

    public <T> T accept(ContextExpressionVisitor<T> visitor) throws PouletException {
        return visitor.visit(this);
    }
}
