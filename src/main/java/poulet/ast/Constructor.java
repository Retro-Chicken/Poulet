package poulet.ast;

public class Constructor {
    Symbol name;
    Expression definition;

    public Constructor(Symbol name, Expression definition) {
        this.name = name;
        this.definition = definition;
    }
}
