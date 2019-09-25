package poulet.kernel.ast;

import poulet.PouletException;
import poulet.kernel.decomposition.AbstractionDecomposition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Fix extends Expression {
    public final List<Clause> clauses;
    public final Symbol mainSymbol;

    // Extended annotations for decreasing arguments
    public final List<Integer> ks;
    public final List<Symbol> xs;

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
    }

    public Fix(List<Clause> clauses, Symbol mainSymbol) {
        this.clauses = clauses;
        this.mainSymbol = mainSymbol;

        // Get decreasing argument information
        List<Integer> ks = new ArrayList<>();
        List<Symbol> xs = new ArrayList<>();
        for (Fix.Clause clause : clauses) {
            // make sure no recursion in types
            for (Fix.Clause otherClause : clauses) {
                if (clause.type.occurs(otherClause.symbol)) {
                    throw new PouletException("no recusion allowed in types of fix clauses");
                }
            }

            AbstractionDecomposition abstractionDecomposition = new AbstractionDecomposition(clause.definition);

            if (!(abstractionDecomposition.body instanceof Match))
                throw new PouletException("body of fix definition must be a match, not a " + abstractionDecomposition.body.getClass().getSimpleName());

            Match match = (Match) abstractionDecomposition.body;

            if (!(match.expression instanceof Var))
                throw new PouletException("body of fix definition must match on an argument");

            Var var = (Var) match.expression;
            Integer k = null;

            for (int i = 0; i < abstractionDecomposition.arguments.size(); i++) {
                Symbol argument = abstractionDecomposition.arguments.get(i);

                if (argument.equals(var.symbol)) {
                    k = i;
                    break;
                }
            }

            if (k == null)
                throw new PouletException("" + var + " isn't an argument to the fix function");

            ks.add(k);
            xs.add(var.symbol);
        }
        this.ks = ks;
        this.xs = xs;
    }

    public Fix(List<Clause> clauses, Symbol mainSymbol, List<Integer> ks, List<Symbol> xs) {
        this.clauses = clauses;
        this.mainSymbol = mainSymbol;
        this.ks = ks;
        this.xs = xs;
    }

    public int getClauseIndex(Symbol clauseSymbol) {
        for(int i = 0; i < clauses.size(); i++) {
            if(clauses.get(i).symbol.equals(clauseSymbol))
                return i;
        }
        throw new PouletException("clause " + clauseSymbol + " not defined in " + this);
    }

    public Clause getMainClause() {
        for (Clause clause : clauses) {
            if (clause.symbol.equals(mainSymbol)) {
                return clause;
            }
        }

        throw new PouletException("main clause " + mainSymbol + " not defined in " + this);
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visit(this);
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
                mainSymbol,
                ks,
                xs
        );
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
    Fix transformSymbols(Function<Symbol, Symbol> transformer, Map<Symbol, Symbol> unique) {
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
}
