package poulet.ast;

import poulet.exceptions.PouletException;

import java.util.Map;

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
    CharLiteral makeSymbolsUnique(Map<Symbol, Symbol> map) {
        return this;
    }

    public <T> T accept(ExpressionVisitor<T> visitor) throws PouletException {
        return visitor.visit(this);
    }
}
