package poulet.kernel.ast;

import java.util.Map;
import java.util.function.Function;

public abstract class Sort extends Expression {
    @Override
    public Expression transformVars(Function<Var, Expression> transformation) {
        return this;
    }

    @Override
    public boolean occurs(Symbol symbol) {
        return false;
    }

    @Override
    Sort transformSymbols(Function<Symbol, Symbol> transformer, Map<Symbol, Symbol> unique) {
        return this;
    }
}
