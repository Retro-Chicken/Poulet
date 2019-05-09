package poulet.typing;

import poulet.ast.Expression;
import poulet.ast.Symbol;
import poulet.ast.Variable;
import poulet.value.Name;
import poulet.value.VType;
import poulet.value.Value;

import java.util.HashMap;
import java.util.Map;

public class Context {
    public final Map<Name, Value> context;

    public Context() {
        this.context = new HashMap<>();
    }

    public Context(Map<Name, Value> context) {
        this.context = context;
    }

    public Context append(Name name, Value type) {
        Map<Name, Value> newContext = new HashMap<>(context);
        newContext.put(name, type);
        return new Context(newContext);
    }

    /**
     * increment all bound variable indices,
     * used when stepping into an abstraction
     */
    public Context increment() {
        Map<Name, Value> newContext = new HashMap<>();
        for (Map.Entry<Name, Value> entry : context.entrySet()) {
            newContext.put(entry.getKey().increment(), entry.getValue());
        }
        return new Context(newContext);
    }

    public Value lookUp(Name name) {
        if(name instanceof Symbol) {
            Symbol symbol = (Symbol) name;
            if (symbol.getName() != null && symbol.getName().matches("Type\\d+")) {
                int level = Integer.parseInt(symbol.getName().substring(4));
                return new VType(level + 1);
            }
        }
        return context.getOrDefault(name, null);
    }

    @Override
    public String toString() {
        return context.toString();
    }
}
