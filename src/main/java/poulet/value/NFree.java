package poulet.value;

import poulet.ast.Symbol;

public class NFree extends Neutral {
    public final Symbol symbol;

    public NFree(Symbol symbol) {
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return symbol.toString();
    }
}
