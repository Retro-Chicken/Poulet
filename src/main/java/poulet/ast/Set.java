package poulet.ast;

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

    public Set(Environment environment) {
        super(environment);
    }

    public Set() {
        this(null);
    }

    public Set context(Environment environment) {
        return new Set(environment);
    }
}
