package poulet.ast;

import poulet.contextexpressions.ContextAbstraction;
import poulet.exceptions.PouletException;
import poulet.typing.Environment;
import poulet.util.ExpressionVisitor;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Abstraction extends Expression {
    public final Symbol symbol;
    public final Expression type;
    public final Expression body;

    public Abstraction(Symbol symbol, Expression type, Expression body) {
        this.symbol = symbol;
        this.type = type;
        this.body = body;
    }

    @Override
    public String toString() {
        return String.format("\\%s : %s -> %s", symbol, type, body);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Abstraction) {
            Abstraction other = (Abstraction) obj;
            if (symbol == null) {
                return other.symbol == null && type.equals(other.type) && body.equals(other.body);
            } else {
                return symbol.equals(other.symbol) && type.equals(other.type) && body.equals(other.body);
            }
        }

        return false;
    }

    @Override
    Abstraction transformSymbols(Function<Symbol, Symbol> transformer, Map<Symbol, Symbol> map) throws PouletException {
        Map<Symbol, Symbol> newMap = new HashMap<>(map);
        Symbol newSymbol = transformer.apply(symbol);
        newMap.put(symbol, newSymbol);

        return new Abstraction(
                newSymbol,
                type.transformSymbols(transformer, map),
                body.transformSymbols(transformer, newMap)
        );
    }

    public <T> T accept(ExpressionVisitor<T> visitor) throws PouletException {
        return visitor.visit(this);
    }

    public ContextAbstraction contextExpression(Environment environment) {
        return new ContextAbstraction(this, environment);
    }
}
