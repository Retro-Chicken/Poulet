package poulet.superficial.ast.expressions;

public class Var extends Expression {
    public final Symbol symbol;

    public Var(Symbol symbol) {
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return "" + symbol;
    }

    @Override
    public boolean occurs(Symbol symbol) {
        return this.symbol.equals(symbol);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Var) {
            Var other = (Var) obj;
            return symbol.equals(other.symbol);
        }

        return false;
    }

    @Override
    public poulet.kernel.ast.Var project() {
        return new poulet.kernel.ast.Var(symbol.project());
    }
}
