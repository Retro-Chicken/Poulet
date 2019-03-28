package poulet.ast;

public class Abstraction extends Expression {
    public Symbol symbol;
    public Expression type;
    public Expression body;

    public Abstraction(Symbol symbol, Expression type, Expression body) {
        this.symbol = symbol;
        this.type = type;
        this.body = body;
    }

    @Override
    public String toString() {
        return String.format("\\%s : %s -> %s", symbol, type, body);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Abstraction) {
            Abstraction other = (Abstraction) obj;
            return symbol.equals(other.symbol) && type.equals(other.type) && body.equals(other.body);
        }

        return false;
    }
}
