package poulet.ast;

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
        String s = "fix {\n";
        s += definitions.stream().map(Definition::toString).collect(Collectors.joining("\n"));
        s += "\n}." + symbol;
        return s;
    }
}
