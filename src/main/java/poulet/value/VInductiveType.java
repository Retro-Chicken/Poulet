package poulet.value;

import poulet.ast.TypeDeclaration;

import java.util.List;

public class VInductiveType extends Value {
    public TypeDeclaration typeDeclaration;
    public List<Value> parameters;
    public List<Value> arguments;

    public VInductiveType(TypeDeclaration typeDeclaration, List<Value> parameters, List<Value> arguments) {
        this.typeDeclaration = typeDeclaration;
        this.parameters = parameters;
        this.arguments = arguments;
    }
}
