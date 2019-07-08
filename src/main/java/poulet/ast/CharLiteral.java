package poulet.ast;

import poulet.contextexpressions.ContextCharLiteral;
import poulet.exceptions.PouletException;
import poulet.typing.Environment;
import poulet.util.ExpressionVisitor;

import java.util.Map;
import java.util.function.Function;

public class CharLiteral extends Expression {
    public final char c;

    public CharLiteral(char c) {
        this.c = c;
    }

    @Override
    public String toString() {
        return "'" + c + "'";
    }

    @Override
    CharLiteral transformSymbols(Function<Symbol, Symbol> transformer, Map<Symbol, Symbol> map) {
        return this;
    }

    public <T> T accept(ExpressionVisitor<T> visitor) throws PouletException {
        return visitor.visit(this);
    }

    public ContextCharLiteral contextExpression(Environment environment) {
        return new ContextCharLiteral(this, environment);
    }
}
