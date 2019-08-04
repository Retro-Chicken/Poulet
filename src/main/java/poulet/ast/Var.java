package poulet.ast;

public class Var extends Expression {
    public Symbol symbol;

    public Var() {
        this.symbol = new Symbol("_");
    }

    public Var(Symbol symbol) {
        this.symbol = symbol;
    }

    @Override
    public poulet.kernel.ast.Expression compile() {
        return new poulet.kernel.ast.Var(symbol.compile());
    }
}
