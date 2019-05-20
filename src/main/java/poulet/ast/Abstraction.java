package poulet.ast;

import poulet.Util;
import poulet.interpreter.Interpreter;
import poulet.typing.Checker;

public class Abstraction extends Expression {
    public final Symbol symbol;
    public final Expression type;
    public final Expression body;

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
            if (symbol == null) {
                return other.symbol == null && type.equals(other.type) && body.equals(other.body);
            } else {
                return symbol.equals(other.symbol) && type.equals(other.type) && body.equals(other.body);
            }
        }

        return false;
    }
}
