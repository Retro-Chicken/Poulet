package poulet.ast;

public class Annotation extends Expression {
    public final Expression expression;
    public final Expression type;

    public Annotation(Expression expression, Expression type) {
        this.expression = expression;
        this.type = type;
    }

    @Override
    public String toString() {
        return new Abstraction(null, type, expression).toString();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Annotation) {
            Annotation other = (Annotation) obj;
            return expression.equals(other.expression) && type.equals(other.type);
        }
        return false;
    }
}
