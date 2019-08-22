package poulet.superficial.ast.expressions;

import poulet.superficial.Desugar;
import poulet.superficial.ast.Inline;
import poulet.util.StringUtil;

import java.util.List;
import java.util.stream.Collectors;

public class Match extends Expression.Projectable {
    public final Expression expression;
    public final Symbol expressionSymbol;
    public final List<Symbol> argumentSymbols;
    public final Expression type;
    public final List<Clause> clauses;

    public static class Clause extends Inline.Projectable {
        public Symbol constructor;
        public List<Symbol> argumentSymbols;
        public Expression expression;

        public Clause(Symbol constructor, List<Symbol> argumentSymbols, Expression expression) {
            this.constructor = constructor;
            this.argumentSymbols = argumentSymbols;
            this.expression = expression;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Clause) {
                Clause other = (Clause) obj;
                return constructor.equals(other.constructor) &&
                        argumentSymbols.equals(other.argumentSymbols) &&
                        expression.equals(other.expression);
            }

            return false;
        }

        @Override
        public String toString() {
            String s = "" + constructor + "(";
            s += argumentSymbols.stream().map(Symbol::toString).collect(Collectors.joining(", "));
            s += ") => " + expression;
            return s;
        }

        @Override
        public poulet.kernel.ast.Match.Clause project() {
            return new poulet.kernel.ast.Match.Clause(
                    constructor.project(),
                    argumentSymbols.stream().map(Symbol::project).collect(Collectors.toList()),
                    Desugar.desugar(expression)
            );
        }
    }

    public Match(Expression expression, Symbol expressionSymbol, List<Symbol> argumentSymbols, Expression type, List<Clause> clauses) {
        this.expression = expression;
        this.expressionSymbol = expressionSymbol;
        this.argumentSymbols = argumentSymbols;
        this.type = type;
        this.clauses = clauses;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Match) {
            Match other = (Match) obj;
            return expression.equals(other.expression) &&
                    expressionSymbol.equals(other.expressionSymbol) &&
                    argumentSymbols.equals(other.argumentSymbols) &&
                    type.equals(other.type) &&
                    clauses.equals(other.clauses);
        }

        return false;
    }

    @Override
    public String toString() {
        String items = clauses.stream().map(Clause::toString).collect(Collectors.joining(",\n"));

        String s = "match " + expression + " as " + expressionSymbol + "(";
        s += argumentSymbols.stream().map(Symbol::toString).collect(Collectors.joining(", "));
        s += ") in " + type + " {\n";
        s += StringUtil.indent(items, 2);
        s += "\n}";
        return s;
    }

    @Override
    public boolean occurs(Symbol symbol) {
        if (expression.occurs(symbol)) {
            return true;
        }

        if (type.occurs(symbol)) {
            return true;
        }

        for (Clause clause : clauses) {
            if (clause.expression.occurs(symbol)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public poulet.kernel.ast.Match project() {
        return new poulet.kernel.ast.Match(
                Desugar.desugar(expression),
                expressionSymbol.project(),
                argumentSymbols.stream().map(Symbol::project).collect(Collectors.toList()),
                Desugar.desugar(type),
                clauses.stream().map(Clause::project).collect(Collectors.toList())
        );
    }
}
