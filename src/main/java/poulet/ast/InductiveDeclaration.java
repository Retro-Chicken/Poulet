package poulet.ast;

import java.util.List;
import java.util.stream.Collectors;

public class InductiveDeclaration extends TopLevel {
    private int nargs;
    private List<TypeDeclaration> typeDeclarations;

    public InductiveDeclaration(int nargs, List<TypeDeclaration> typeDeclarations) {
        this.nargs = nargs;
        this.typeDeclarations = typeDeclarations;
    }

    @Override
    public String toString() {
        String s = String.format("inductive %d {\n", nargs);
        s += typeDeclarations.stream()
                .map(t -> "  " + t.toString())
                .collect(Collectors.joining("\n"));
        s += "\n}";
        return s;
    }
}
