package poulet.superficial.ast.expressions;

public class Prop extends Sort {
    @Override
    public String toString() {
        return "Prop";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Prop;
    }

    @Override
    public poulet.kernel.ast.Prop project() {
        return new poulet.kernel.ast.Prop();
    }
}
