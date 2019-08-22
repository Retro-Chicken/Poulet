package poulet.superficial.ast.multilines;

import poulet.parser.SuperficialNode;
import poulet.superficial.imports.ImportHandler;

import java.util.List;
import java.util.stream.Collectors;

public class Import extends Multiline {
    public final String importName;
    public final List<String> subSections;

    public Import(String importName, List<String> subSections) {
        this.importName = importName;
        this.subSections = subSections;
    }

    @Override
    public List<SuperficialNode> inflate() {
        return ImportHandler.expand(this).nodes;
    }

    @Override
    public String toString() {
        return "open " + importName + (subSections.isEmpty() ? "" : ".") + subSections.stream().collect(Collectors.joining("."));
    }
}
