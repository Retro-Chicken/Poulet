package poulet.ast;

import java.util.List;

public class InductiveDeclaration extends TopLevel {
    public List<TypeDeclaration> typeDeclarations;

    public InductiveDeclaration(List<TypeDeclaration> typeDeclarations) {
        this.typeDeclarations = typeDeclarations;
    }
}
