package poulet.kernel.ast;

public class Definition extends TopLevel {
    public final Symbol name;
    public final Expression type;
    public final Expression definition;

    public Definition(Symbol name, Expression type, Expression definition) {
        this.name = name;
        this.type = type;
        this.definition = definition;
    }

    public Definition(Symbol name, Expression type) {
        this(name, type, null);
    }

    @Override
    public TopLevel makeSymbolsUnique() {
        Expression uniqueDefinition = definition == null ? null : definition.makeSymbolsUnique();
        return new Definition(name, type.makeSymbolsUnique(), uniqueDefinition);
    }

    @Override
    public String toString() {
        if (definition == null) {
            return "" + name + " : " + type;
        } else {

            return "" + name + " : " + type + " := " + definition;
        }
    }
}
