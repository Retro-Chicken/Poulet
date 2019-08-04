package poulet.ast;

import java.util.List;

public class TypeDeclaration extends Node {
    public InductiveDeclaration inductiveDeclaration;
    public Symbol name;
    public List<Parameter> parameters;
    public Expression type;
    public List<Constructor> constructors;

    public static class Parameter extends Node {
        public Symbol name;
        public Expression type;

        public Parameter(Symbol name, Expression type) {
            this.name = name;
            this.type = type;
        }
    }

    public static class Constructor extends Node {
        public Symbol name;
        public Expression definition;

        public Constructor(Symbol name, Expression definition) {
            this.name = name;
            this.definition = definition;
        }
    }

    public TypeDeclaration(InductiveDeclaration inductiveDeclaration, Symbol name, List<Parameter> parameters, Expression type, List<Constructor> constructors) {
        this.inductiveDeclaration = inductiveDeclaration;
        this.name = name;
        this.parameters = parameters;
        this.type = type;
        this.constructors = constructors;
    }
}
