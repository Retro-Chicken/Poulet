package poulet.ast;

public abstract class Expression extends Node {
    Expression readableExpression() {
        return this;
    }
    public String readableString() {
        return readableExpression().toString();
    }
}
