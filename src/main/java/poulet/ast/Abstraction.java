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
}
