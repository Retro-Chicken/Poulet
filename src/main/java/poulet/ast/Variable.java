package poulet.ast;

import poulet.exceptions.PouletException;

import java.util.Map;

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
    Variable makeSymbolsUnique(Map<Symbol, Symbol> map) {
        if (isFree()) {
            return this;
        } else {
            Symbol unique = map.get(symbol);

            if (unique == null) {
                return new Variable(symbol.makeUnique());
            } else {
                return new Variable(unique.copy());
            }
        }
    }

    public <T> T accept(ExpressionVisitor<T> visitor) throws PouletException {
        return visitor.visit(this);
    }
}
