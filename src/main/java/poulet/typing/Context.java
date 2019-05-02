package poulet.typing;

import poulet.ast.Expression;
import poulet.ast.Symbol;

import java.util.HashMap;

public class Context {
    public final HashMap<Symbol, Expression> context;

    public Context() {
        this.context = new HashMap<>();
    }

    public Context(HashMap<Symbol, Expression> context) {
        this.context = context;
    }

    public Context append(Symbol name, Expression type) {
        HashMap<Symbol, Expression> newContext = new HashMap<>(context);
        newContext.put(name, type);
        return new Context(newContext);
    }

    public Expression lookUp(Symbol name) {
        return context.getOrDefault(name, null);
    }
}
