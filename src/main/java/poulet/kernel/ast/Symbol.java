package poulet.kernel.ast;

import poulet.parser.KernelNode;

public class Symbol extends KernelNode implements Comparable<Symbol> {
    public final String symbol;

    public Symbol(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return symbol;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Symbol) {
            Symbol other = (Symbol) obj;
            return other.symbol.equals(symbol);
        }

        return false;
    }

    @Override
    public int compareTo(Symbol symbol) {
        return this.symbol.compareTo(symbol.symbol);
    }

    @Override
    public int hashCode() {
        return symbol.hashCode();
    }

    Symbol rename(String symbol) {
        return new Symbol(symbol);
    }
}
