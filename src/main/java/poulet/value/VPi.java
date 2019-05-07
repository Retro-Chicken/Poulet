package poulet.value;

import java.util.function.Function;

public class VPi extends Value {
    public final Value type;
    public final Function<Value, Value> body;

    public VPi(Value type, Function<Value, Value> body) {
        this.type = type;
        this.body = body;
    }
}
