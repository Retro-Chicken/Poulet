package poulet.superficial.ast;

import poulet.kernel.ast.TopLevel;
import poulet.superficial.imports.ImportHandler;

import java.util.List;
import java.util.stream.Collectors;

public class Import extends Sugar {
    public final String importName;
    public final List<String> subSections;

    public Import(String importName, List<String> subSections) {
        this.importName = importName;
        this.subSections = subSections;
    }

    @Override
    public List<TopLevel> inflate() {
        return ImportHandler.expand(this).project().topLevels;
    }

    @Override
    public String toString() {
        return "open " + importName + (subSections.isEmpty() ? "" : ".") + subSections.stream().collect(Collectors.joining("."));
    }
}
