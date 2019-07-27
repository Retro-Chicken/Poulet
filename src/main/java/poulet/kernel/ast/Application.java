package poulet.kernel.ast;

import java.util.Map;
import java.util.function.Function;

public class Application extends Expression {
    public final Expression function;
    public final Expression argument;

    public Application(Expression function, Expression argument) {
        this.function = function;
        this.argument = argument;
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public Application transformVars(Function<Var, Expression> transformation) {
        return new Application(
                function.transformVars(transformation),
                argument.transformVars(transformation)
        );
    }

    @Override
    public boolean occurs(Symbol symbol) {
        return function.occurs(symbol) || argument.occurs(symbol);
    }

    @Override
    public String toString() {
        if (function instanceof Abstraction) {
            return "(" + function + ")(" + argument + ")";
        } else {
            return "" + function + "(" + argument + ")";
        }
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
    Application transformSymbols(Function<Symbol, Symbol> transformer, Map<Symbol, Symbol> unique) {
        return new Application(
                function.transformSymbols(transformer, unique),
                argument.transformSymbols(transformer, unique)
        );
    }
}
