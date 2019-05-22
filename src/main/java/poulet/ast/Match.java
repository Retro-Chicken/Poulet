package poulet.ast;

import poulet.Util;
import poulet.exceptions.PouletException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Override
    Match makeSymbolsUnique(Map<Symbol, Symbol> map) throws PouletException {
        Map<Symbol, Symbol> newMap = new HashMap<>(map);
        Symbol newExpressionSymbol = expressionSymbol.makeUnique();
        newMap.put(expressionSymbol, newExpressionSymbol);

        List<Symbol> newArgumentSymbols = new ArrayList<>();
        for (Symbol symbol : argumentSymbols) {
            Symbol unique = symbol.makeUnique();
            newArgumentSymbols.add(unique);
            newMap.put(symbol, unique);
        }

        Expression newType = type.makeSymbolsUnique(newMap);

        List<Match.Clause> newClauses = new ArrayList<>();

        for (Match.Clause clause : clauses) {
            Map<Symbol, Symbol> clauseNewMap = new HashMap<>(map);

            List<Symbol> clauseArgumentSymbols = new ArrayList<>();
            for (Symbol symbol : clause.argumentSymbols) {
                Symbol unique = symbol.makeUnique();
                clauseArgumentSymbols.add(unique);
                clauseNewMap.put(symbol, unique);
            }

            Match.Clause newClause = new Match.Clause(
                    clause.constructorSymbol,
                    clauseArgumentSymbols,
                    clause.expression.makeSymbolsUnique(clauseNewMap)
            );
            newClauses.add(newClause);
        }

        return new Match(
                expression.makeSymbolsUnique(map),
                newExpressionSymbol,
                newArgumentSymbols,
                newType,
                newClauses
        );
    }

    public <T> T accept(ExpressionVisitor<T> visitor) throws PouletException {
        return visitor.visit(this);
    }
}
