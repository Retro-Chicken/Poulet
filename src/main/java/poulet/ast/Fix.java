package poulet.ast;

import poulet.Util;

import java.util.List;
import java.util.stream.Collectors;

public class Fix extends Expression {
    public final List<Definition> definitions;
    public final Symbol symbol;

    public Fix(List<Definition> definitions, Symbol symbol) {
        this.definitions = definitions;
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        String items = definitions.stream().map(Definition::toString).collect(Collectors.joining("\n"));
        return "fix {\n" + Util.indent(items, 2) + "\n}." + symbol;
    }
}
