package poulet.superficial.ast.expressions;

import poulet.superficial.Desugar;
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

    @Override
    public poulet.kernel.ast.InductiveDeclaration project() {
        List<poulet.kernel.ast.TypeDeclaration> declarations = new ArrayList<>();
        for(TypeDeclaration declaration : typeDeclarations) {
            declarations.add(new poulet.kernel.ast.TypeDeclaration(
                    null,
                    declaration.name.project(),
                    declaration.parameters.stream().map(TypeDeclaration.Parameter::project).collect(Collectors.toList()),
                    Desugar.desugar(declaration.type),
                    declaration.constructors.stream().map(TypeDeclaration.Constructor::project).collect(Collectors.toList())
            ));
        }
        poulet.kernel.ast.InductiveDeclaration result = new poulet.kernel.ast.InductiveDeclaration(declarations);
        for(poulet.kernel.ast.TypeDeclaration declaration : result.typeDeclarations)
            declaration.inductiveDeclaration = result;
        return result;
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
}
