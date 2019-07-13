package poulet.ast;

import poulet.exceptions.PouletException;
import poulet.typing.Environment;
import poulet.util.ExpressionVisitor;

import java.util.Map;
import java.util.function.Function;

public class Application extends Expression {
    public final Expression function;
    public final Expression argument;

    public Application(Expression function, Expression argument, Environment environment) throws PouletException {
        super(environment);
        this.function = function.context(environment);
        this.argument = argument.context(environment);
    }

    public Application(Expression function, Expression argument) throws PouletException {
        this(function, argument, null);
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
    Application transformSymbols(Function<Symbol, Symbol> transformer, Map<Symbol, Symbol> map) throws PouletException {
        return new Application(
                function.transformSymbols(transformer, map),
                argument.transformSymbols(transformer, map),
                environment
        );
    }

    public <T> T accept(ExpressionVisitor<T> visitor) throws PouletException {
        return visitor.visit(this);
    }

    public Application context(Environment environment) throws PouletException {
        return new Application(function, argument, environment);
    }
}
