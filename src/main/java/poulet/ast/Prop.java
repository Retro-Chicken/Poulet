package poulet.ast;

import poulet.contextexpressions.ContextProp;
import poulet.exceptions.PouletException;
import poulet.typing.Environment;
import poulet.util.ExpressionVisitor;

public class Prop extends Sort {
    @Override
    public String toString() {
        return "Prop";
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) throws PouletException {
        return visitor.visit(this);
    }

    public ContextProp contextExpression(Environment environment) {
        return new ContextProp(this, environment);
    }
}
