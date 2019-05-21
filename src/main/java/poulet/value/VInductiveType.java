package poulet.value;

import poulet.ast.TypeDeclaration;

import java.util.List;

public class VInductiveType extends Value {
    public TypeDeclaration typeDeclaration;
    public boolean concrete;
    public List<Value> parameters;
    public List<Value> arguments;

    public VInductiveType(TypeDeclaration typeDeclaration, boolean concrete, List<Value> parameters, List<Value> arguments) {
        this.typeDeclaration = typeDeclaration;
        this.concrete = concrete;
        this.parameters = parameters;
        this.arguments = arguments;
    }
}
