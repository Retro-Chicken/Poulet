package poulet.kernel.ast;

import poulet.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InductiveDeclaration extends TopLevel {
    public final List<TypeDeclaration> typeDeclarations;

    public InductiveDeclaration(List<TypeDeclaration> typeDeclarations) {
        this.typeDeclarations = typeDeclarations;
    }

    @Override
    public InductiveDeclaration makeSymbolsUnique() {
        List<TypeDeclaration> unique = new ArrayList<>();

        for (TypeDeclaration typeDeclaration : typeDeclarations) {
            unique.add(typeDeclaration.makeSymbolsUnique());
        }

        InductiveDeclaration inductiveDeclaration = new InductiveDeclaration(unique);

        for (TypeDeclaration typeDeclaration : inductiveDeclaration.typeDeclarations) {
            typeDeclaration.inductiveDeclaration = inductiveDeclaration;
        }

        return inductiveDeclaration;
    }

    @Override
    public String toString() {
        if (typeDeclarations.size() == 1) {
            return typeDeclarations.get(0).toString();
        } else {
            String s = "inductive {\n";
            String types = typeDeclarations.stream().map(TypeDeclaration::toString).collect(Collectors.joining("\n"));
            s += StringUtil.indent(types, 2);
            s += "\n}";
            return s;
        }
    }
}
