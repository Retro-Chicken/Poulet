package poulet.kernel.ast;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public abstract class Expression extends Node {
    public abstract <T> T accept(ExpressionVisitor<T> visitor);

    // recursively replace Var(name) with transformation(name)
    public abstract Expression transformVars(Function<Var, Expression> transformation);

    public Expression substitute(Symbol symbol, Expression substitution) {
        return transformVars(var -> {
            if (var.symbol.equals(symbol)) {
                return substitution;
            } else {
                return var;
            }
        });
    }

    public Expression makeSymbolsUnique() {
        return transformSymbols(UniqueSymbol::new, new HashMap<>());
    }

    // recursively transform bound variables (e.g., \x : T -> f(x) becomes \x' : T -> f(x') where x' = transformer.apply(x))
    Expression transformSymbols(Function<Symbol, Symbol> transformer) {
        return transformSymbols(transformer, new HashMap<>());
    }

    abstract Expression transformSymbols(Function<Symbol, Symbol> transformer, Map<Symbol, Symbol> unique);

    // assign unique symbols starting from 0, used for comparison
    public Expression normalizeUniqueSymbols() {
        int oldNextId = UniqueSymbol.nextId;
        UniqueSymbol.nextId = 0;
        Expression normalized = transformSymbols(symbol ->
                new UniqueSymbol(new Symbol("_"))
        );
        UniqueSymbol.nextId = oldNextId;
        return normalized;
    }

    public abstract boolean occurs(Symbol symbol);
}
