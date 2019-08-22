package poulet.superficial.ast.expressions;

public class Type extends Sort {
    public final int level;

    public Type(int level) {
        this.level = level;
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return "Type" + level;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Type) {
            Type other = (Type) obj;
            return level == other.level;
        }

        return false;
    }

    @Override
    public poulet.kernel.ast.Type project() {
        return new poulet.kernel.ast.Type(level);
    }
}
