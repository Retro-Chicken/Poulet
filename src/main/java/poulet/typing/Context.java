package poulet.typing;

import poulet.ast.Expression;
import poulet.ast.Symbol;
import poulet.ast.Variable;

import java.util.HashMap;
import java.util.Map;

public class Context {
    public final Map<Symbol, Expression> context;

    public Context() {
        this.context = new HashMap<>();
    }

    public Context(Map<Symbol, Expression> context) {
        this.context = context;
    }

    public Context append(Symbol name, Expression type) {
        Map<Symbol, Expression> newContext = new HashMap<>(context);
        newContext.put(name, type);
        return new Context(newContext);
    }

    /**
     * increment all bound variable indices,
     * used when stepping into an abstraction
     */
    public Context increment() {
        Map<Symbol, Expression> newContext = new HashMap<>();
        for (Map.Entry<Symbol, Expression> entry : context.entrySet()) {
            newContext.put(entry.getKey().increment(), entry.getValue());
        }
        return new Context(newContext);
    }

    public Expression lookUp(Symbol symbol) {
        if (symbol.getName() != null && symbol.getName().matches("Type\\d+")) {
            int level = Integer.parseInt(symbol.getName().substring(4));
            return new Variable(new Symbol("Type" + (level + 1)));
        }
        return context.getOrDefault(symbol, null);
    }

    @Override
    public String toString() {
        return context.toString();
    }
}
