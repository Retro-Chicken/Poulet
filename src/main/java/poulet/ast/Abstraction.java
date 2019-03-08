package poulet.ast;

public class Abstraction extends Expression {
    Symbol variable;
    Expression type;
    Expression body;

    public Abstraction(Symbol variable, Expression type, Expression body) {
        this.variable = variable;
        this.type = type;
        this.body = body;
    }

    @Override
    public String toString() {
        return String.format("\\%s : %s -> %s", variable, type, body);
    }
}
