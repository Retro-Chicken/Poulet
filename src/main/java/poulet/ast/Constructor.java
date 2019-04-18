package poulet.ast;

public class Constructor extends Node {
    public final Symbol name;
    public final Expression definition;

    public Constructor(Symbol name, Expression definition) {
        this.name = name;
        this.definition = definition;
    }

    @Override
    public String toString() {
        return String.format("%s : %s", name, definition);
    }
}
