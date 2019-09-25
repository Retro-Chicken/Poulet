package poulet.superficial.ast.expressions;

public class Set extends Sort {
    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return "Set";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Set;
    }

    @Override
    public poulet.kernel.ast.Set project() {
        return new poulet.kernel.ast.Set();
    }
}
