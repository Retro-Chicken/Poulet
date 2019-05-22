package poulet.ast;

import poulet.exceptions.PouletException;

import java.util.Map;

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
    public String toString() {
        if (definition == null)
            return String.format("%s : %s", name, type);
        else
            return String.format("%s : %s := %s", name, type, definition);
    }

    @Override
    Definition makeSymbolsUnique(Map<Symbol, Symbol> map) throws PouletException {
        if (definition == null) {
            return new Definition(
                    name,
                    type.makeSymbolsUnique()
            );
        } else {
            return new Definition(
                    name,
                    type.makeSymbolsUnique(),
                    definition.makeSymbolsUnique()
            );
        }
    }

    @Override
    public <T> T accept(TopLevelVisitor<T> visitor) throws PouletException {
        return visitor.visit(this);
    }
}
