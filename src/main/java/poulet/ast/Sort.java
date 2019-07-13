package poulet.ast;

import poulet.typing.Environment;

import java.util.Map;
import java.util.function.Function;

public abstract class Sort extends Expression {
    Expression transformSymbols(Function<Symbol, Symbol> transformer, Map<Symbol, Symbol> map) {
        return this;
    }

    public Sort(Environment environment) {
        super(environment);
    }
}
