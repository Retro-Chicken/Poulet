package poulet.value;

import poulet.ast.Definition;
import poulet.ast.Symbol;

import java.util.List;

public class VFix extends Value {
    public final List<Definition> definitions;
    public final Symbol symbol;

    public VFix(List<Definition> definitions, Symbol symbol) {
        this.definitions = definitions;
        this.symbol = symbol;
    }
}
