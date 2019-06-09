package poulet.ast;

import java.util.Map;
import java.util.function.Function;

public abstract class Sort extends Expression {
    Expression transformSymbols(Function<Symbol, Symbol> transformer, Map<Symbol, Symbol> map) {
        return this;
    }
}
