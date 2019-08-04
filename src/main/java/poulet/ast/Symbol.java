package poulet.ast;

public class Symbol extends Node {
    public final String symbol;

    public Symbol(String symbol) {
        this.symbol = symbol;
    }

    public poulet.kernel.ast.Symbol compile() {
       return new poulet.kernel.ast.Symbol(symbol);
    }
}
