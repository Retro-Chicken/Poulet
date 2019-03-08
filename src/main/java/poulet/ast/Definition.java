package poulet.ast;

public class Definition extends TopLevel {
    Symbol name;
    Expression type;
    Expression definition;

    public Definition(Symbol name, Expression type, Expression definition) {
        this.name = name;
        this.type = type;
        this.definition = definition;
    }

    public Definition(Symbol name, Expression type) {
        this(name, type, null);
    }

    @Override
    public String toString() {
        if (definition == null)
            return String.format("%s : %s", name, type);
        else
            return String.format("%s : %s := %s", name, type, definition);
    }
}
