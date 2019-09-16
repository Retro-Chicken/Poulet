package poulet.superficial.ast.expressions;

import java.util.Map;
import java.util.function.Function;

public class MetaVar extends Expression.Projectable {
    public final Symbol symbol;

    public MetaVar(Symbol symbol) {
        this.symbol = symbol;
    }

    @Override
    public Expression transformVars(Function<Var, Expression> transformation) {
        return this;
    }

    @Override
    public MetaVar transformSymbols(Function<Symbol, Symbol> transformer, Map<Symbol, Symbol> unique) {
        return this;
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return "?" + symbol;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MetaVar) {
            MetaVar other = (MetaVar) obj;
            return symbol.equals(other.symbol);
        }

        return false;
    }

    @Override
    public boolean occurs(Symbol symbol) {
        return false;
    }

    @Override
    public poulet.kernel.ast.MetaVar project() {
        return new poulet.kernel.ast.MetaVar(symbol.project());
    }
}
