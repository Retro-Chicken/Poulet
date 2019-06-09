package poulet.ast;

import poulet.exceptions.PouletException;
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
}
