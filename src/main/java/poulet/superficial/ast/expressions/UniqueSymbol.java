package poulet.superficial.ast.expressions;

public class UniqueSymbol extends Symbol {
    public final int id;
    static int nextId = 0;

    // placeholder symbol
    public UniqueSymbol() {
        this(new Symbol("_"));
    }

    public UniqueSymbol(Symbol symbol) {
        super(symbol.symbol);
        this.id = symbol instanceof UniqueSymbol ? ((UniqueSymbol) symbol).id : nextId++;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UniqueSymbol) {
            UniqueSymbol other = (UniqueSymbol) obj;
            return other.symbol.equals(symbol) && other.id == id;
        }

        return false;
    }

    @Override
    public String toString() {
        return symbol + "@" + id;
    }

    @Override
    public poulet.kernel.ast.UniqueSymbol project() {
        return new poulet.kernel.ast.UniqueSymbol(symbol, id);
    }
}
