package poulet.kernel.ast;

public class Set extends Sort {
    @Override
    public String toString() {
        return "Set";
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Set;
    }
}
