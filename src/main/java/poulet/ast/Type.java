package poulet.ast;

public class Type extends Sort {
    public Integer level;

    public Type(int level) {
        this.level = level;
    }

    public Type() {
        this.level = null;
    }

    @Override
    public poulet.kernel.ast.Expression compile() {
        // TODO: decide how to implement level inference
        return new poulet.kernel.ast.Type(level);
    }
}
