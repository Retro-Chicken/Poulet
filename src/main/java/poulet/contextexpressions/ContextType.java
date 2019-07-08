package poulet.contextexpressions;

import poulet.ast.Type;
import poulet.exceptions.PouletException;
import poulet.typing.Environment;
import poulet.util.ContextExpressionVisitor;

public class ContextType extends ContextExpression {
    public final int level;

    public ContextType(Type type, Environment environment) throws PouletException {
        super(type, environment);
        this.level = type.level;
    }

    public ContextType(int level, Environment environment) throws PouletException {
        this(new Type(level), environment);
    }

    public <T> T accept(ContextExpressionVisitor<T> visitor) throws PouletException {
        return visitor.visit(this);
    }
}
