package poulet.superficial.ast.inlines;

import poulet.superficial.Substituter;
import poulet.superficial.ast.expressions.Expression;
import poulet.superficial.ast.expressions.ExpressionVisitor;
import poulet.superficial.ast.expressions.Symbol;
import poulet.superficial.ast.expressions.Var;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class LetIn extends Expression.Transformable {
    public Symbol symbol;
    public Expression value;
    public Expression body;

    public LetIn(Symbol symbol, Expression value, Expression body) {
        this.symbol = symbol;
        this.body = body;
        this.value = value;
    }

    @Override
    public LetIn transformVars(Function<Var, Expression> transformation) {
        return new LetIn(
                symbol,
                value.transformVars(transformation),
                body.transformVars(transformation)
        );
    }

    @Override
    public LetIn transformSymbols(Function<Symbol, Symbol> transformer, Map<Symbol, Symbol> unique) {
        Map<Symbol, Symbol> newUnique = new HashMap<>(unique);
        Symbol uniqueArgumentSymbol = transformer.apply(symbol);
        newUnique.put(symbol, uniqueArgumentSymbol);

        return new LetIn(
                uniqueArgumentSymbol,
                value.transformSymbols(transformer, unique),
                body.transformSymbols(transformer, newUnique)
        );
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return "let " + symbol + " := " + value + " in " + body;
    }

    @Override
    public boolean occurs(Symbol symbol) {
        return value.occurs(symbol) || body.occurs(symbol);
    }

    @Override
    public Expression transform() {
        return Substituter.substitute(body, symbol, value);
    }
}
