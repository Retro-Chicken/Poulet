package poulet.typing;

import poulet.ast.Expression;
import poulet.ast.Symbol;
import poulet.ast.Variable;
import poulet.value.Name;

import java.util.HashMap;
import java.util.Map;

public class EContext {
    public final Map<Name, Expression> context;

    public EContext() {
        this.context = new HashMap<>();
    }

    public EContext(Map<Name, Expression> context) {
        this.context = context;
    }

    public EContext append(Name name, Expression type) {
        Map<Name, Expression> newContext = new HashMap<>(context);
        newContext.put(name, type);
        return new EContext(newContext);
    }

    /**
     * increment all bound variable indices,
     * used when stepping into an abstraction
     */
    public EContext increment() {
        return offset(1);
    }

    public EContext decrement() {
        return offset(-1);
    }

    public EContext offset(int offset) {
        Map<Name, Expression> newContext = new HashMap<>();
        for (Map.Entry<Name, Expression> entry : context.entrySet()) {
            newContext.put(entry.getKey().offset(offset), entry.getValue());//.offset(offset));
        }
        return new EContext(newContext);
    }

    public Expression lookUp(Name name) {
        if(name instanceof Symbol) {
            Symbol symbol = (Symbol) name;
            if (symbol.getName() != null && symbol.getName().matches("Type\\d+")) {
                int level = Integer.parseInt(symbol.getName().substring(4));
                return new Variable(new Symbol("Type" + (level + 1)));
            }
        }
        return context.getOrDefault(name, null);
    }

    @Override
    public String toString() {
        return context.toString();
    }
}
