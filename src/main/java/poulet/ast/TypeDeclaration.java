package poulet.ast;

import java.util.List;
import java.util.stream.Collectors;

public class TypeDeclaration extends Node {
    public final Symbol name;
    public final List<Parameter> parameters;
    public final Expression type;
    public final List<Constructor> constructors;

    public TypeDeclaration(Symbol name, List<Parameter> parameters, Expression type, List<Constructor> constructors) {
        this.name = name;
        this.parameters = parameters;
        this.type = type;
        this.constructors = constructors;
    }

    @Override
    public String toString() {
        String s = "type " + name + " ";
        s += parameters.stream().map(Parameter::toString).collect(Collectors.joining(" "));
        s += " : " + type + " {\n";
        s += constructors.stream()
                .map(c -> "  " + c.toString())
                .collect(Collectors.joining("\n"));
        s += "\n}";
        return s;
    }
}
