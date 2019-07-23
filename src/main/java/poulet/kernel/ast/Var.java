package poulet.kernel.ast;

import java.util.Map;
import java.util.function.Function;

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
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public Expression transformVars(Function<Var, Expression> transformation) {
        return transformation.apply(this);
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
    Var transformSymbols(Function<Symbol, Symbol> transformer, Map<Symbol, Symbol> unique) {
        if (unique.containsKey(symbol)) {
            return new Var(unique.get(symbol));
        } else {
            return this;
        }
    }
}
