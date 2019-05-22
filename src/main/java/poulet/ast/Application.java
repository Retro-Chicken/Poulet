package poulet.ast;

import poulet.exceptions.PouletException;

import java.util.Map;

public class Application extends Expression {
    public final Expression function;
    public final Expression argument;

    public Application(Expression function, Expression argument) {
        this.function = function;
        this.argument = argument;
    }

    @Override
    public String toString() {
        return String.format("%s(%s)", function, argument);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Application) {
            Application other = (Application) obj;
            return function.equals(other.function) && argument.equals(other.argument);
        }

        return false;
    }

    @Override
    Application makeSymbolsUnique(Map<Symbol, Symbol> map) throws PouletException {
        return new Application(
                function.makeSymbolsUnique(map),
                argument.makeSymbolsUnique(map)
        );
    }

    public <T> T accept(ExpressionVisitor<T> visitor) throws PouletException {
        return visitor.visit(this);
    }
}
