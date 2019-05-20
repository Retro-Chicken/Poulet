package poulet.ast;

public class Variable extends Expression {
    public final Symbol symbol;

    public Variable(Symbol symbol) {
        this.symbol = symbol;
    }

    public boolean isFree() {
        return symbol.id == null;
    }

    @Override
    public String toString() {
        return "" + symbol;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Variable) {
            Variable other = (Variable) obj;
            return symbol.equals(other.symbol);
        }

        return false;
    }
}
