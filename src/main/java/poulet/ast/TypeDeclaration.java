package poulet.ast;

import poulet.Util;

import java.util.List;
import java.util.stream.Collectors;

public class TypeDeclaration extends TopLevel {
    public final Symbol name;
    public final List<Parameter> parameters;
    public final Expression type;
    public final List<Constructor> constructors;
    private final InductiveDeclaration inductiveDeclaration;

    public TypeDeclaration(Symbol name, List<Parameter> parameters, Expression type, List<Constructor> constructors) {
        this.name = name;
        this.parameters = parameters;
        this.type = type;
        this.constructors = constructors;
        this.inductiveDeclaration = null;
    }

    public TypeDeclaration(Symbol name, List<Parameter> parameters, Expression type, List<Constructor> constructors, InductiveDeclaration inductiveDeclaration) {
        this.name = name;
        this.parameters = parameters;
        this.type = type;
        this.constructors = constructors;
        this.inductiveDeclaration = inductiveDeclaration;
    }

    public TypeDeclaration addInductiveDeclaration(InductiveDeclaration inductiveDeclaration) {
        return new TypeDeclaration(name, parameters, type, constructors, inductiveDeclaration);
    }

    public boolean isMutual() {
        return inductiveDeclaration.typeDeclarations.size() > 1;
    }

    @Override
    public String toString() {
        String items = constructors.stream().map(Constructor::toString).collect(Collectors.joining("\n"));
        String s = "type " + name + " ";
        s += parameters.stream().map(Parameter::toString).collect(Collectors.joining(" "));
        s += " : " + type + " {\n";
        s += Util.indent(items, 2);
        s += "\n}";
        return s;
    }
}
