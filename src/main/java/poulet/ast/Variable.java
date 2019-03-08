package poulet.ast;

public class Variable extends Expression {
    Symbol type;
    Symbol name;

    public Variable(Symbol name) {
        this(null, name);
    }

    public Variable(Symbol type, Symbol name) {
        this.type = type;
        this.name = name;
    }

    @Override
    public String toString() {
        if (type == null)
            return name.toString();
        else
            return String.format("%s.%s", type, name);
    }
}
