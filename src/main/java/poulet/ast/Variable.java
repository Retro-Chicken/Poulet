package poulet.ast;

import poulet.value.Name;

public class Variable extends Expression {
    public final Symbol type;
    public final Name symbol;

    public Variable(Name symbol) {
        this(null, symbol);
    }

    public Variable(Symbol type, Name symbol) {
        this.type = type;
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        if (type == null)
            return symbol.toString();
        else
            return String.format("%s.%s", type, symbol);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Variable) {
            Variable other = (Variable) obj;
            if (type == null && other.type == null) {
                return symbol.equals(other.symbol);
            } else if (type != null && other.type != null) {
                return type.equals(other.type) && symbol.equals(other.symbol);
            }
        }

        return false;
    }

    @Override
    public Variable readableExpression() {
        return this;
    }
}
