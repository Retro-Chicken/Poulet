package poulet.value;

import java.util.function.Function;

public class VAbstraction extends Value {
    private final Function<Value, Value> abstraction;

    public VAbstraction(Function<Value, Value> abstraction) {
        this.abstraction = abstraction;
    }

    public Value call(Value argument) {
        return abstraction.apply(argument);
    }
}
