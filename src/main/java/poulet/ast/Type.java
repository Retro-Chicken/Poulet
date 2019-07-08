package poulet.ast;

import poulet.contextexpressions.ContextType;
import poulet.exceptions.PouletException;
import poulet.typing.Environment;
import poulet.util.ExpressionVisitor;

public class Type extends Sort {
    public final int level;

    public Type(int level) {
        this.level = level;
    }

    @Override
    public String toString() {
        return "Type" + level;
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) throws PouletException {
        return visitor.visit(this);
    }

    public ContextType contextExpression(Environment environment) {
        return new ContextType(this, environment);
    }
}