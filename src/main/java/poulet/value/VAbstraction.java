package poulet.value;

import java.util.function.Function;

public class VAbstraction extends Value {
    private final Function<Value, Value> abstraction;

    public final Value type;

    public VAbstraction(Function<Value, Value> abstraction, Value type) {
        this.abstraction = abstraction;
        this.type = type;
    }

    public Value call(Value argument) {
        return abstraction.apply(argument);
    }
}
