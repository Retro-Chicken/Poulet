package poulet.ast;

import poulet.exceptions.PouletException;
import poulet.util.ExpressionVisitor;

import java.util.Map;
import java.util.function.Function;

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

    @Override
    Variable transformSymbols(Function<Symbol, Symbol> transformer, Map<Symbol, Symbol> map) {
        Symbol newSymbol = map.get(symbol);

        if (newSymbol == null) {
            return this;
        } else {
            return new Variable(newSymbol);
        }
    }

    public <T> T accept(ExpressionVisitor<T> visitor) throws PouletException {
        return visitor.visit(this);
    }
}
