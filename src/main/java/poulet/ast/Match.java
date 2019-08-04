package poulet.ast;

import static poulet.util.ListUtil.map;

import java.util.List;

public class Match extends Expression {
    public Expression expression;
    public Symbol expressionSymbol;
    public List<Symbol> argumentSymbols;
    public Expression type;
    public List<Clause> clauses;

    public static class Clause extends Node {
        public Symbol constructor;
        public List<Symbol> argumentSymbols;
        public Expression expression;

        public Clause(Symbol constructor, List<Symbol> argumentSymbols, Expression expression) {
            this.constructor = constructor;
            this.argumentSymbols = argumentSymbols;
            this.expression = expression;
        }

        public poulet.kernel.ast.Match.Clause compile() {
            return new poulet.kernel.ast.Match.Clause(
                    constructor.compile(),
                    map(argumentSymbols, Symbol::compile),
                    expression.compile()
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
    public poulet.kernel.ast.Expression compile() {
        return new poulet.kernel.ast.Match(
                expression.compile(),
                expressionSymbol.compile(),
                map(argumentSymbols, Symbol::compile),
                type.compile(),
                map(clauses, Clause::compile)
        );
    }
}
