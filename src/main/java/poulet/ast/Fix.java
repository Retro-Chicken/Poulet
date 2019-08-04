package poulet.ast;

import static poulet.util.ListUtil.map;

import java.util.List;

public class Fix extends Expression {
    public List<Clause> clauses;
    public Symbol mainSymbol;

    public static class Clause {
        public Symbol symbol;
        public Expression type;
        public Expression definition;

        public Clause(Symbol symbol, Expression type, Expression definition) {
            this.symbol = symbol;
            this.type = type;
            this.definition = definition;
        }

        public poulet.kernel.ast.Fix.Clause compile() {
            return new poulet.kernel.ast.Fix.Clause(
                    symbol.compile(),
                    type.compile(),
                    definition.compile()
            );
        }
    }

    public Fix(List<Clause> clauses, Symbol mainSymbol) {
        this.clauses = clauses;
        this.mainSymbol = mainSymbol;
    }

    @Override
    public poulet.kernel.ast.Expression compile() {
        return new poulet.kernel.ast.Fix(
                map(clauses, Clause::compile),
                mainSymbol.compile()
        );
    }
}
