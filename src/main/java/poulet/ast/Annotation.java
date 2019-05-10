package poulet.ast;

public class Annotation extends Expression {
    private Expression expression;
    private Expression type;

    public Annotation(Expression expression, Expression type) {
        this.expression = expression;
        this.type = type;
    }
}
