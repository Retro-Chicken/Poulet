package poulet.ast;

import poulet.Util;
import poulet.exceptions.PouletException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    @Override
    InductiveDeclaration makeSymbolsUnique(Map<Symbol, Symbol> map) throws PouletException {
        List<TypeDeclaration> newTypeDeclarations = new ArrayList<>();

        for (TypeDeclaration typeDeclaration : typeDeclarations) {
            newTypeDeclarations.add(typeDeclaration.makeSymbolsUnique());
        }

        return new InductiveDeclaration(newTypeDeclarations);
    }

    @Override
    public <T> T accept(TopLevelVisitor<T> visitor) throws PouletException {
        return visitor.visit(this);
    }
}
