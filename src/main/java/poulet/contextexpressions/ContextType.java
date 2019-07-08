package poulet.contextexpressions;

import poulet.ast.Type;
import poulet.exceptions.PouletException;
import poulet.typing.Environment;
import poulet.util.ContextExpressionVisitor;

public class ContextType extends ContextExpression {
    public final int level;

    public ContextType(Type type, Environment environment) {
        super(type, environment);
        this.level = type.level;
    }

    public <T> T accept(ContextExpressionVisitor<T> visitor) throws PouletException {
        return visitor.visit(this);
    }
}
