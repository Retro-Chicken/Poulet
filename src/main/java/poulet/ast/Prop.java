package poulet.ast;

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

    public Prop(Environment environment) {
        super(environment);
    }

    public Prop() {
        this(null);
    }

    public Prop context(Environment environment) {
        return new Prop(environment);
    }
}
