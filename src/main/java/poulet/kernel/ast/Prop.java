package poulet.kernel.ast;

public class Prop extends Sort {
    @Override
    public String toString() {
        return "Prop";
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Prop;
    }
}
