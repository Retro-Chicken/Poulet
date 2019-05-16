package poulet.ast;

import java.util.List;
import java.util.stream.Collectors;

public class InductiveDeclaration extends TopLevel {
    private List<TypeDeclaration> typeDeclarations;

    public InductiveDeclaration(List<TypeDeclaration> typeDeclarations) {
        this.typeDeclarations = typeDeclarations;
    }

    @Override
    public String toString() {
        String s = "inductive {\n";
        s += typeDeclarations.stream()
                .map(TypeDeclaration::toString)
                .collect(Collectors.joining("\n"));
        s += "\n}";
        return s;
    }
}
