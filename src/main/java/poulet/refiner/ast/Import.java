package poulet.refiner.ast;

import poulet.parser.Node;

import java.util.List;
import java.util.stream.Collectors;

public class Import extends Node {
    public final String importName;
    public final List<String> subSections;

    public Import(String importName, List<String> subSections) {
        this.importName = importName;
        this.subSections = subSections;
    }

    @Override
    public String toString() {
        return "open " + importName + (subSections.isEmpty() ? "" : ".") + subSections.stream().collect(Collectors.joining("."));
    }
}
