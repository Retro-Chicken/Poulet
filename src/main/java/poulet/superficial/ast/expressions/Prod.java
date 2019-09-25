package poulet.superficial.ast.expressions;

import poulet.superficial.Desugar;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Prod extends Expression.Projectable {
    public Symbol argumentSymbol;
    public Expression argumentType;
    public Expression bodyType;

    // non-dependent arrow type
    public Prod(Expression argumentType, Expression bodyType) {
        this.argumentSymbol = new Symbol("_");
        this.argumentType = argumentType;
        this.bodyType = bodyType;
    }

    public Prod(Symbol argumentSymbol, Expression argumentType, Expression bodyType) {
        this.argumentSymbol = argumentSymbol;
        this.argumentType = argumentType;
        this.bodyType = bodyType;
    }

    @Override
    public Expression transformVars(Function<Var, Expression> transformation) {
        return new Prod(
                argumentSymbol,
                argumentType.transformVars(transformation),
                bodyType.transformVars(transformation)
        );
    }

    @Override
    public Expression transformSymbols(Function<Symbol, Symbol> transformer, Map<Symbol, Symbol> unique) {
        Map<Symbol, Symbol> newUnique = new HashMap<>(unique);
        Symbol uniqueArgumentSymbol = transformer.apply(argumentSymbol);
        newUnique.put(argumentSymbol, uniqueArgumentSymbol);

        return new Prod(
                uniqueArgumentSymbol,
                argumentType.transformSymbols(transformer, unique),
                bodyType.transformSymbols(transformer, newUnique)
        );
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Prod) {
            Prod other = (Prod) obj;
            return argumentSymbol.equals(other.argumentSymbol) &&
                    argumentType.equals(other.argumentType) &&
                    bodyType.equals(other.bodyType);
        }

        return false;
    }

    boolean isDependent() {
        return bodyType.occurs(argumentSymbol);
    }

    @Override
    public String toString() {
        if (isDependent()) {
            return "{" + argumentSymbol + " : " + argumentType + "} " + bodyType;
        } else {
            return "" + argumentType + " -> " + bodyType;
        }
    }

    @Override
    public boolean occurs(Symbol symbol) {
        return argumentType.occurs(symbol) || bodyType.occurs(symbol);
    }

    @Override
    public poulet.kernel.ast.Prod project() {
        return new poulet.kernel.ast.Prod(
                argumentSymbol.project(),
                Desugar.desugar(argumentType),
                Desugar.desugar(bodyType)
        );
    }
}
