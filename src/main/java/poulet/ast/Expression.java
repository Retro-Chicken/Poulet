package poulet.ast;

public abstract class Expression extends Node {
    abstract Expression readableExpression();
    public String readableString() {
        return readableExpression().toString();
    }
}
