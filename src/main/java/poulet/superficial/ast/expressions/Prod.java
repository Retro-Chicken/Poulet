package poulet.superficial.ast.expressions;

import poulet.superficial.Desugar;

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
