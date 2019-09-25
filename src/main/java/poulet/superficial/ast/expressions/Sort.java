package poulet.superficial.ast.expressions;

import java.util.Map;
import java.util.function.Function;

public abstract class Sort extends Expression.Projectable {
    @Override
    public Expression transformVars(Function<Var, Expression> transformation) {
        return this;
    }

    @Override
    public Sort transformSymbols(Function<Symbol, Symbol> transformer, Map<Symbol, Symbol> unique) {
        return this;
    }

    public boolean occurs(Symbol symbol) {
        return false;
    }
}
