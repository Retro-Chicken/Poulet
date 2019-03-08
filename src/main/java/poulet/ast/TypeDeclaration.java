package poulet.ast;

import java.util.List;
import java.util.stream.Collectors;

public class TypeDeclaration extends TopLevel {
    Symbol name;
    Expression type;
    List<Constructor> constructors;

    public TypeDeclaration(Symbol name, Expression type, List<Constructor> constructors) {
        this.name = name;
        this.type = type;
        this.constructors = constructors;
    }

    @Override
    public String toString() {
        String s = String.format("type %s : %s {\n", name, type);
        s += constructors.stream()
                .map(c -> "  " + c.toString())
                .collect(Collectors.joining("\n"));
        s += "\n}";
        return s;
    }
}
