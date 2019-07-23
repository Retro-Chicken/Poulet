package poulet.kernel.ast;

import java.util.ArrayList;
import java.util.List;

public class InductiveDeclaration extends TopLevel {
    public final List<TypeDeclaration> typeDeclarations;

    public InductiveDeclaration(List<TypeDeclaration> typeDeclarations) {
        this.typeDeclarations = typeDeclarations;
    }

    @Override
    public TopLevel makeSymbolsUnique() {
        List<TypeDeclaration> unique = new ArrayList<>();

        for (TypeDeclaration typeDeclaration : typeDeclarations) {
            unique.add(typeDeclaration.makeSymbolsUnique());
        }

        return new InductiveDeclaration(unique);
    }
}
