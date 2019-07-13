package poulet.ast;

import poulet.exceptions.PouletException;
import poulet.typing.Environment;
import poulet.util.ExpressionVisitor;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class PiType extends Expression {
    public final Symbol variable;
    public final Expression type;
    public final Expression body;

    public final boolean inferable;

    public PiType(Symbol variable, Expression type, Expression body, boolean inferable, Environment environment) throws PouletException {
        super(environment);
        this.variable = variable;
        this.type = type.context(environment);
        this.body = body.context(environment == null ? null : environment.appendType(variable, type));
        this.inferable = inferable;
    }

    public PiType(Symbol variable, Expression type, Expression body, Environment environment) throws PouletException {
        this(variable, type, body, false, environment);
    }

    public PiType(Symbol variable, Expression type, Expression body, boolean inferable) throws PouletException {
        this(variable, type, body, inferable, null);
    }

    public PiType(Symbol variable, Expression type, Expression body) throws PouletException {
        this(variable, type, body, null);
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
                body.transformSymbols(transformer, newMap),
                inferable,
                environment
        );
    }

    public <T> T accept(ExpressionVisitor<T> visitor) throws PouletException {
        return visitor.visit(this);
    }

    public PiType context(Environment environment) throws PouletException {
        return new PiType(variable, type, body, inferable, environment);
    }
}
