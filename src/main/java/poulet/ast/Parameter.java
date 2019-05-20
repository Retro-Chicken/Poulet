package poulet.ast;

public class Parameter extends Node {
    public final Symbol symbol;
    public final Expression type;

    public Parameter(Symbol symbol, Expression type) {
        this.symbol = symbol;
        this.type = type;
    }

    @Override
    public String toString() {
        return "(" + symbol + ":" + type + ")";
    }
}
