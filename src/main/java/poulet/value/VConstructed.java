package poulet.value;

import poulet.ast.Constructor;

import java.util.List;

public class VConstructed extends Value {
    public final VInductiveType inductiveType;
    public final List<Value> parameters;
    public final Constructor constructor;
    public final List<Value> arguments;

    public VConstructed(VInductiveType inductiveType, List<Value> parameters, Constructor constructor, List<Value> arguments) {
        this.inductiveType = inductiveType;
        this.parameters = parameters;
        this.constructor = constructor;
        this.arguments = arguments;
    }
}
