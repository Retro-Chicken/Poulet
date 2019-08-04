package poulet.ast;

public abstract class Expression extends Node {
    public abstract poulet.kernel.ast.Expression compile();
}
