package poulet.kernel.ast;

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

    private UniqueSymbol(String symbol, int id) {
        super(symbol);
        this.id = id;
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
    Symbol rename(String symbol) {
        return new UniqueSymbol(symbol, id);
    }
}
