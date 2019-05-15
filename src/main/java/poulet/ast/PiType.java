package poulet.ast;

import poulet.Util;
import poulet.typing.Checker;

public class PiType extends Expression {
    public final Symbol variable;
    public final Expression type;
    public final Expression body;

    public PiType(Symbol variable, Expression type, Expression body) {
        this.variable = variable;
        this.type = type;
        this.body = body;
    }

    @Override
    public String toString() {
        return String.format("{%s : %s} %s", variable == null ? Util.NULL_PITYPE_SYMBOL : variable, type, body);
    }

    @Override
    Expression readableExpression() {
        Symbol uniqueSymbol = Util.getReadableSymbol();
        return new PiType(uniqueSymbol, type.readableExpression(), Checker.substitute(body, new Variable(uniqueSymbol)).readableExpression());
    }
}
