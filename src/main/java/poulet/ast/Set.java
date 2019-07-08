package poulet.ast;

import poulet.contextexpressions.ContextSet;
import poulet.exceptions.PouletException;
import poulet.typing.Environment;
import poulet.util.ExpressionVisitor;

public class Set extends Sort {
    @Override
    public String toString() {
        return "Set";
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) throws PouletException {
        return visitor.visit(this);
    }

    public ContextSet contextExpression(Environment environment) throws PouletException {
        return new ContextSet(this, environment);
    }
}
