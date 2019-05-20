package poulet.ast;

import poulet.Util;

import java.util.List;
import java.util.stream.Collectors;

public class InductiveDeclaration extends TopLevel {
    public final List<TypeDeclaration> typeDeclarations;

    public InductiveDeclaration(List<TypeDeclaration> typeDeclarations) {
        this.typeDeclarations = typeDeclarations;
    }

    @Override
    public String toString() {
        if (typeDeclarations.size() == 1) {
            return typeDeclarations.get(0).toString();
        } else {
            String items = typeDeclarations.stream()
                    .map(TypeDeclaration::toString)
                    .collect(Collectors.joining("\n"));
            return "inductive {\n" + Util.indent(items, 2) + "\n}";
        }
    }
}
