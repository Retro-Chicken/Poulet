package poulet.superficial.ast.expressions;

import poulet.superficial.Desugar;

public class Abstraction extends Expression.Projectable {
    public final Symbol argumentSymbol;
    public final Expression argumentType;
    public final Expression body;

    public Abstraction(Symbol argumentSymbol, Expression argumentType, Expression body) {
        this.argumentSymbol = argumentSymbol;
        this.argumentType = argumentType;
        this.body = body;
    }

    @Override
    public String toString() {
        if (argumentType instanceof Prod) {
            Prod prod = (Prod) argumentType;
            if (!prod.isDependent()) {
                return "\\" + argumentSymbol + " : (" + argumentType + ") -> " + body;
            }
        }
        return "\\" + argumentSymbol + " : " + argumentType + " -> " + body;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Abstraction) {
            Abstraction other = (Abstraction) obj;
            return argumentSymbol.equals(other.argumentSymbol) &&
                    argumentType.equals(other.argumentType) &&
                    body.equals(other.body);
        }

        return false;
    }

    @Override
    public boolean occurs(Symbol symbol) {
        return argumentType.occurs(symbol) || body.occurs(symbol);
    }

    @Override
    public poulet.kernel.ast.Abstraction project() {
        return new poulet.kernel.ast.Abstraction(
                argumentSymbol.project(),
                Desugar.desugar(argumentType),
                Desugar.desugar(body)
        );
    }
}
