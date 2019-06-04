package poulet.ast;

import poulet.util.StringUtil;
import poulet.exceptions.PouletException;
import poulet.util.ExpressionVisitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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

    public Clause getClause(Symbol constructor) throws PouletException {
        for (Clause clause : clauses) {
            if (clause.constructorSymbol.equals(constructor)) {
                return clause;
            }
        }

        throw new PouletException("no clause for " + constructor + " in " + this);
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
    Match transformSymbols(Function<Symbol, Symbol> transformer, Map<Symbol, Symbol> map) throws PouletException {
        Map<Symbol, Symbol> newMap = new HashMap<>(map);
        Symbol newExpressionSymbol = transformer.apply(expressionSymbol);
        newMap.put(expressionSymbol, newExpressionSymbol);

        List<Symbol> newArgumentSymbols = new ArrayList<>();
        for (Symbol argumentSymbol : argumentSymbols) {
            Symbol newArgumentSymbol = transformer.apply(argumentSymbol);
            newArgumentSymbols.add(newArgumentSymbol);
            newMap.put(argumentSymbol, newArgumentSymbol);
        }

        Expression newType = type.transformSymbols(transformer, newMap);

        List<Match.Clause> newClauses = new ArrayList<>();

        for (Match.Clause clause : clauses) {
            Map<Symbol, Symbol> clauseNewMap = new HashMap<>(map);

            List<Symbol> clauseArgumentSymbols = new ArrayList<>();
            for (Symbol clauseArgumentSymbol : clause.argumentSymbols) {
                Symbol newClauseArgumentSymbol = transformer.apply(clauseArgumentSymbol);
                clauseArgumentSymbols.add(newClauseArgumentSymbol);
                clauseNewMap.put(clauseArgumentSymbol, newClauseArgumentSymbol);
            }

            Match.Clause newClause = new Match.Clause(
                    clause.constructorSymbol,
                    clauseArgumentSymbols,
                    clause.expression.transformSymbols(transformer, clauseNewMap)
            );
            newClauses.add(newClause);
        }

        return new Match(
                expression.transformSymbols(transformer, map),
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
