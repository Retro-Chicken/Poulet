package poulet.superficial.ast.multilines;

import poulet.parser.SuperficialNode;
import poulet.superficial.imports.ImportHandler;

import java.util.List;
import java.util.stream.Collectors;

public class Import extends Multiline {
    public final String importName;
    public final List<String> subSections;

    private boolean FLAG_UNIQUE = false;

    public Import(String importName, List<String> subSections) {
        this.importName = importName;
        this.subSections = subSections;
    }

    @Override
    public List<SuperficialNode> inflate() {
        List<SuperficialNode> result = ImportHandler.expand(this).nodes;
        if(FLAG_UNIQUE)
            return result.stream().map(x -> x.makeSymbolsUnique()).collect(Collectors.toList());
        else
            return result;
    }

    @Override
    public Import makeSymbolsUnique() {
        FLAG_UNIQUE = true;
        return this;
    }

    @Override
    public String toString() {
        return "open " + importName + (subSections.isEmpty() ? "" : ".") + subSections.stream().collect(Collectors.joining("."));
    }
}
