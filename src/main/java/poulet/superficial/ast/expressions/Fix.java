package poulet.superficial.ast.expressions;

import poulet.superficial.Desugar;

import java.util.List;
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
