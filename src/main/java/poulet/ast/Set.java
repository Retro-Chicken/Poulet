package poulet.ast;

public class Set extends Sort {
    @Override
    public poulet.kernel.ast.Expression compile() {
        return new poulet.kernel.ast.Set();
    }
}
