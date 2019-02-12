package poulet.ast;

public class Variable extends Expression {
    Symbol name;

    public Variable(Symbol name) {
        this.name = name;
    }
}
