package poulet.ast;

import java.util.List;

public class TypeDeclaration extends TopLevel {
    Symbol name;
    Expression type;
    List<Constructor> constructors;

    public TypeDeclaration(Symbol name, Expression type, List<Constructor> constructors) {
        this.name = name;
        this.type = type;
        this.constructors = constructors;
    }
}
