package poulet.superficial.ast.expressions;

import poulet.superficial.Desugar;
import poulet.superficial.decomposition.ApplicationDecomposition;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Application extends Expression.Projectable {
    public final Expression function;
    public final Expression argument;

    public Application(Expression function, Expression argument) {
        this.function = function;
        this.argument = argument;
    }

    @Override
    public Application transformVars(Function<Var, Expression> transformation) {
        return new Application(
                function.transformVars(transformation),
                argument.transformVars(transformation)
        );
    }

    @Override
    public Application transformSymbols(Function<Symbol, Symbol> transformer, Map<Symbol, Symbol> unique) {
        return new Application(
                function.transformSymbols(transformer, unique),
                argument.transformSymbols(transformer, unique)
        );
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        ApplicationDecomposition applicationDecomposition = new ApplicationDecomposition(this);
        String arguments = applicationDecomposition.arguments.stream().map(Objects::toString).collect(Collectors.joining(", "));

        if (applicationDecomposition.function instanceof Abstraction) {
            return "(" + applicationDecomposition.function + ")(" + arguments + ")";
        } else {
            return "" + applicationDecomposition.function + "(" + arguments + ")";
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
    public boolean occurs(Symbol symbol) {
        return function.occurs(symbol) || argument.occurs(symbol);
    }

    @Override
    public poulet.kernel.ast.Expression project() {
        return new poulet.kernel.ast.Application(
                Desugar.desugar(function),
                Desugar.desugar(argument)
        );
    }
}
