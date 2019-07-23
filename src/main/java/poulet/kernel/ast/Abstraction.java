package poulet.kernel.ast;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Abstraction extends Expression {
    public final Symbol argumentSymbol;
    public final Expression argumentType;
    public final Expression body;

    public Abstraction(Symbol argumentSymbol, Expression argumentType, Expression body) {
        this.argumentSymbol = argumentSymbol;
        this.argumentType = argumentType;
        this.body = body;
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visit(this);
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
    public boolean occurs(Symbol symbol) {
        return argumentType.occurs(symbol) || body.occurs(symbol);
    }

    @Override
    public String toString() {
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
    Abstraction transformSymbols(Function<Symbol, Symbol> transformer, Map<Symbol, Symbol> unique) {
        Map<Symbol, Symbol> newUnique = new HashMap<>(unique);
        Symbol uniqueArgumentSymbol = transformer.apply(argumentSymbol);
        newUnique.put(argumentSymbol, uniqueArgumentSymbol);

        return new Abstraction(
                uniqueArgumentSymbol,
                argumentType.transformSymbols(transformer, unique),
                body.transformSymbols(transformer, newUnique)
        );
    }
}
