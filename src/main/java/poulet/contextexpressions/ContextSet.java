package poulet.contextexpressions;

import poulet.ast.Set;
import poulet.exceptions.PouletException;
import poulet.typing.Environment;
import poulet.util.ContextExpressionVisitor;

public class ContextSet extends ContextExpression {
    public ContextSet(Set set, Environment environment) throws PouletException {
        super(set, environment);
    }

    public ContextSet(Environment environment) throws PouletException {
        this(new Set(), environment);
    }

    public <T> T accept(ContextExpressionVisitor<T> visitor) throws PouletException {
        return visitor.visit(this);
    }
}
