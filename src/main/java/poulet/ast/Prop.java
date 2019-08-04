package poulet.ast;

public class Prop extends Sort {
    @Override
    public poulet.kernel.ast.Expression compile() {
        return new poulet.kernel.ast.Prop();
    }
}
