package poulet.ast;

public class PiType extends Expression {
    Symbol variable;
    Expression type;
    Expression body;

    public PiType(Symbol variable, Expression type, Expression body) {
        this.variable = variable;
        this.type = type;
        this.body = body;
    }
}
