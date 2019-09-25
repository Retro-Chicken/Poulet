package poulet.superficial.ast.expressions;

import poulet.PouletException;
import poulet.superficial.Desugar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Fix extends Expression.Projectable {
    public final List<Clause> clauses;
    public final Symbol mainSymbol;

    public static class Clause {
        public final Symbol symbol;
        public final Expression type;
        public final Expression definition;

        public Clause(Symbol symbol, Expression type, Expression definition) {
            this.symbol = symbol;
            this.type = type;
            this.definition = definition;
        }

        @Override
        public String toString() {
            return "" + symbol + " : " + type + " := " + definition;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Clause) {
                Clause other = (Clause) obj;

                return symbol.equals(other.symbol) &&
                        type.equals(other.type) &&
                        definition.equals(other.definition);
            }

            return false;
        }

        public poulet.kernel.ast.Fix.Clause project() {
            return new poulet.kernel.ast.Fix.Clause(
                    symbol.project(),
                    Desugar.desugar(type),
                    Desugar.desugar(definition)
            );
        }
    }

    public Fix(List<Clause> clauses, Symbol mainSymbol) {
        this.clauses = clauses;
        this.mainSymbol = mainSymbol;
    }

    @Override
    public Fix transformVars(Function<Var, Expression> transformation) {
        List<Clause> newClauses = new ArrayList<>();

        for (Clause clause : clauses) {
            newClauses.add(new Clause(
                    clause.symbol,
                    clause.type.transformVars(transformation),
                    clause.definition.transformVars(transformation)
            ));
        }

        return new Fix(
                clauses,
                mainSymbol
        );
    }

    @Override
    public Fix transformSymbols(Function<Symbol, Symbol> transformer, Map<Symbol, Symbol> unique) {
        Symbol uniqueMainSymbol = null;
        Map<Symbol, Symbol> newUnique = new HashMap<>(unique);

        for (Clause clause : clauses) {
            Symbol uniqueSymbol = transformer.apply(clause.symbol);
            newUnique.put(clause.symbol, uniqueSymbol);

            if (clause.symbol.equals(mainSymbol)) {
                uniqueMainSymbol = uniqueSymbol;
            }
        }

        if (uniqueMainSymbol == null) {
            throw new PouletException("function " + mainSymbol + " not defined in " + this);
        }

        List<Clause> newClauses = new ArrayList<>();

        for (Clause clause : clauses) {
            Clause newClause = new Clause(
                    newUnique.get(clause.symbol),
                    clause.type.transformSymbols(transformer, unique),
                    clause.definition.transformSymbols(transformer, unique)
            );
            newClauses.add(newClause);
        }

        return new Fix(newClauses, uniqueMainSymbol);
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        String clausesString = clauses.stream().map(Clause::toString).collect(Collectors.joining("\n"));
        return "fix {\n" + clausesString + "\n}." + mainSymbol;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Fix) {
            Fix other = (Fix) obj;
            return clauses.equals(other.clauses) &&
                    mainSymbol.equals(other.mainSymbol);
        }

        return false;
    }

    @Override
    public boolean occurs(Symbol symbol) {
        for (Clause clause : clauses) {
            if (clause.type.occurs(symbol) || clause.definition.occurs(symbol)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public poulet.kernel.ast.Fix project() {
        return new poulet.kernel.ast.Fix(
            clauses.stream().map(Clause::project).collect(Collectors.toList()),
            mainSymbol.project()
        );
    }
}
