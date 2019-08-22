package poulet.kernel.ast;

import poulet.PouletException;
import poulet.parser.KernelNode;
import poulet.util.StringUtil;

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

    public static class Clause extends KernelNode {
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
    }

    public Match(Expression expression, Symbol expressionSymbol, List<Symbol> argumentSymbols, Expression type, List<Clause> clauses) {
        this.expression = expression;
        this.expressionSymbol = expressionSymbol;
        this.argumentSymbols = argumentSymbols;
        this.type = type;
        this.clauses = clauses;
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public Match transformVars(Function<Var, Expression> transformation) {
        List<Clause> newClauses = new ArrayList<>();

        for (Clause clause : clauses) {
            newClauses.add(new Clause(
                   clause.constructor,
                   clause.argumentSymbols,
                   clause.expression.transformVars(transformation)
            ));
        }

        return new Match(
                expression.transformVars(transformation),
                expressionSymbol,
                argumentSymbols,
                type.transformVars(transformation),
                newClauses
        );
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

    public Clause getClause(Symbol name) {
        for (Clause clause : clauses) {
            if (clause.constructor.equals(name)) {
                return clause;
            }
        }

        throw new PouletException("clause for constructor " + name + " not found in " + this);
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
    Match transformSymbols(Function<Symbol, Symbol> transformer, Map<Symbol, Symbol> unique) {
        Map<Symbol, Symbol> newUnique = new HashMap<>(unique);
        Symbol newExpressionSymbol = transformer.apply(expressionSymbol);
        newUnique.put(expressionSymbol, newExpressionSymbol);

        List<Symbol> newArgumentSymbols = new ArrayList<>();
        for (Symbol argumentSymbol : argumentSymbols) {
            Symbol newArgumentSymbol = transformer.apply(argumentSymbol);
            newArgumentSymbols.add(newArgumentSymbol);
            newUnique.put(argumentSymbol, newArgumentSymbol);
        }

        Expression newType = type.transformSymbols(transformer, newUnique);

        List<Match.Clause> newClauses = new ArrayList<>();

        for (Match.Clause clause : clauses) {
            Map<Symbol, Symbol> clauseNewUnique = new HashMap<>(unique);

            List<Symbol> clauseArgumentSymbols = new ArrayList<>();
            for (Symbol clauseArgumentSymbol : clause.argumentSymbols) {
                Symbol newClauseArgumentSymbol = transformer.apply(clauseArgumentSymbol);
                clauseArgumentSymbols.add(newClauseArgumentSymbol);
                clauseNewUnique.put(clauseArgumentSymbol, newClauseArgumentSymbol);
            }

            Match.Clause newClause = new Match.Clause(
                    clause.constructor,
                    clauseArgumentSymbols,
                    clause.expression.transformSymbols(transformer, clauseNewUnique)
            );
            newClauses.add(newClause);
        }

        return new Match(
                expression.transformSymbols(transformer, unique),
                newExpressionSymbol,
                newArgumentSymbols,
                newType,
                newClauses
        );
    }
}
