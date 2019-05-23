package poulet.ast;

import poulet.exceptions.PouletException;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class PiType extends Expression {
    public final Symbol variable;
    public final Expression type;
    public final Expression body;

    public PiType(Symbol variable, Expression type, Expression body) {
        this.variable = variable;
        this.type = type;
        this.body = body;
    }

    @Override
    public String toString() {
        return String.format("{%s : %s} %s", variable, type, body);
    }

    @Override
    PiType transformSymbols(Function<Symbol, Symbol> transformer, Map<Symbol, Symbol> map) throws PouletException {
        Map<Symbol, Symbol> newMap = new HashMap<>(map);
        Symbol newVariable = transformer.apply(variable);
        newMap.put(variable, newVariable);

        return new PiType(
                newVariable,
                type.transformSymbols(transformer, map),
                body.transformSymbols(transformer, newMap)
        );
    }

    public <T> T accept(ExpressionVisitor<T> visitor) throws PouletException {
        return visitor.visit(this);
    }
}
