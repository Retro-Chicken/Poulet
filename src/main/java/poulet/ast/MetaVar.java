package poulet.ast;

public class MetaVar extends Expression {
    public Symbol symbol;

    public MetaVar(Symbol symbol) {
        this.symbol = symbol;
    }

    public MetaVar() {
        this.symbol = new Symbol("");
    }

    @Override
    public poulet.kernel.ast.Expression compile() {
        return new poulet.kernel.ast.MetaVar(symbol.compile());
    }
}
