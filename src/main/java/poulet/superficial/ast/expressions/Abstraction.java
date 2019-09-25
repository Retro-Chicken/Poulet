package poulet.superficial.ast.expressions;

import poulet.superficial.Desugar;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

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
    public Abstraction transformVars(Function<Var, Expression> transformation) {
        return new Abstraction(
                argumentSymbol,
                argumentType.transformVars(transformation),
                body.transformVars(transformation)
        );
    }

    @Override
    public Abstraction transformSymbols(Function<Symbol, Symbol> transformer, Map<Symbol, Symbol> unique) {
        Map<Symbol, Symbol> newUnique = new HashMap<>(unique);
        Symbol uniqueArgumentSymbol = transformer.apply(argumentSymbol);
        newUnique.put(argumentSymbol, uniqueArgumentSymbol);

        return new Abstraction(
                uniqueArgumentSymbol,
                argumentType.transformSymbols(transformer, unique),
                body.transformSymbols(transformer, newUnique)
        );
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visit(this);
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
