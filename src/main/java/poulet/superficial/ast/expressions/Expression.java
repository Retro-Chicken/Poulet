package poulet.superficial.ast.expressions;

import poulet.superficial.ast.inlines.Inline;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public abstract class Expression extends Inline {
    public abstract boolean occurs(Symbol symbol);

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

    public Expression makeSymbolsUnique(Map<Symbol, Symbol> unique) {
        return transformSymbols(UniqueSymbol::new, unique);
    }

    // recursively transform bound variables (e.g., \x : T -> f(x) becomes \x' : T -> f(x') where x' = transformer.apply(x))
    public Expression transformSymbols(Function<Symbol, Symbol> transformer) {
        return transformSymbols(transformer, new HashMap<>());
    }

    public abstract Expression transformSymbols(Function<Symbol, Symbol> transformer, Map<Symbol, Symbol> unique);


    public static abstract class Transformable extends Expression {
        public abstract Expression transform();
    }
    public static abstract class Projectable extends Expression {
        public abstract poulet.kernel.ast.Expression project();
    }
}
