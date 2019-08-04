package poulet.ast;

public class Definition extends TopLevel {
    public Symbol name;
    public Expression type;
    public Expression definition;

    public Definition(Symbol name, Expression type, Expression definition) {
        this.name = name;
        this.type = type;
        this.definition = definition;
    }

    public Definition(Symbol name, Expression type) {
        this(name, type, null);
    }
}
