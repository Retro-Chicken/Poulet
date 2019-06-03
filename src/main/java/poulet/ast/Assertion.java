package poulet.ast;

import poulet.exceptions.PouletException;
import poulet.util.TopLevelVisitor;

import java.util.Map;

public class Assertion extends TopLevel {
    public final Expression a, b;

    public Assertion(Expression a, Expression b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public String toString() {
        return "#assert " + a + " ~ " + b;
    }

    @Override
    Assertion makeSymbolsUnique(Map<Symbol, Symbol> map) throws PouletException {
        return new Assertion(
            a.makeSymbolsUnique(),
            b.makeSymbolsUnique()
        );
    }

    @Override
    public <T> T accept(TopLevelVisitor<T> visitor) throws PouletException {
        return visitor.visit(this);
    }
}
