package poulet.kernel.context;

import poulet.kernel.ast.Expression;
import poulet.kernel.ast.Symbol;
import poulet.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

public abstract class Context {
    final Map<Symbol, Expression> assumptions;
    final Map<Symbol, Expression> definitions;

    Context() {
        this.assumptions = new HashMap<>();
        this.definitions = new HashMap<>();
    }

    Context(Map<Symbol, Expression> assumptions, Map<Symbol, Expression> definitions) {
        this.assumptions = assumptions;
        this.definitions = definitions;
    }

    // copy constructor
    Context(Context context) {
        assumptions = new HashMap<>(context.assumptions);
        definitions = new HashMap<>(context.definitions);
    }

    public void assume(Symbol symbol, Expression expression) {
        assumptions.put(symbol, expression);
    }

    public void define(Symbol symbol, Expression definition) {
        definitions.put(symbol, definition);
    }

    public void define(Symbol symbol, Expression type, Expression definition) {
        assumptions.put(symbol, type);
        definitions.put(symbol, definition);
    }

    public Expression getAssumption(Symbol symbol) {
        return assumptions.getOrDefault(symbol, null);
    }

    public Expression getDefinition(Symbol symbol) {
        return definitions.getOrDefault(symbol, null);
    }

    @Override
    public String toString() {
        return StringUtil.mapToStringWithNewlines(Map.ofEntries(
                Map.entry("assumptions", assumptions),
                Map.entry("definitions", definitions)
        ));
    }
}
