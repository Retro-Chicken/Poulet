package poulet.value;

import java.util.function.Function;

public class VAbstraction extends Value {
    private final Function<Value, Value> abstraction;

    public final Value type;

    public VAbstraction(Value type, Function<Value, Value> abstraction) {
        this.abstraction = abstraction;
        this.type = type;
    }

    public Value call(Value argument) {
        return abstraction.apply(argument);
    }
}
