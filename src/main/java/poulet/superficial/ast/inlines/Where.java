package poulet.superficial.ast.inlines;

import poulet.superficial.Substituter;
import poulet.superficial.ast.expressions.Expression;
import poulet.superficial.ast.expressions.ExpressionVisitor;
import poulet.superficial.ast.expressions.Symbol;

public class Where extends Expression.Transformable {
    public Symbol symbol;
    public Expression value;
    public Expression body;

    public Where(Symbol symbol, Expression value, Expression body) {
        this.symbol = symbol;
        this.body = body;
        this.value = value;
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return "" + body + " where " + symbol + " := " + value;
    }

    @Override
    public boolean occurs(Symbol symbol) {
        return value.occurs(symbol) || body.occurs(symbol);
    }

    @Override
    public Expression transform() {
        return Substituter.substitute(body, symbol, value);
    }
}
