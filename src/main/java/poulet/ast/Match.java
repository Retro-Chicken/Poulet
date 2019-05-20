package poulet.ast;

import poulet.Util;

import java.util.List;
import java.util.stream.Collectors;

public class Match extends Expression {
    public final Expression expression;
    public final Symbol expressionSymbol;
    public final List<Symbol> argumentSymbols;
    public final Expression type;
    public final List<Clause> clauses;

    public Match(Expression expression, Symbol expressionSymbol, List<Symbol> argumentSymbols, Expression type, List<Clause> clauses) {
        this.expression = expression;
        this.expressionSymbol = expressionSymbol;
        this.argumentSymbols = argumentSymbols;
        this.type = type;
        this.clauses = clauses;
    }

    @Override
    public String toString() {
        String items = clauses.stream().map(Clause::toString).collect(Collectors.joining(",\n"));

        String s = "match " + expression + " as " + expressionSymbol + "(";
        s += argumentSymbols.stream().map(Symbol::toString).collect(Collectors.joining(", "));
        s += ") in " + type + " {\n";
        s += Util.indent(items, 2);
        s += "\n}";
        return s;
    }

    public static class Clause extends Node {
        public final Symbol constructorSymbol;
        public final List<Symbol> argumentSymbols;
        public final Expression expression;

        public Clause(Symbol constructorSymbol, List<Symbol> argumentSymbols, Expression expression) {
            this.constructorSymbol = constructorSymbol;
            this.argumentSymbols = argumentSymbols;
            this.expression = expression;
        }

        @Override
        public String toString() {
            String s = "" + constructorSymbol + "(";
            s += argumentSymbols.stream().map(Symbol::toString).collect(Collectors.joining(", "));
            s += ") => " + expression;
            return s;
        }
    }
}
