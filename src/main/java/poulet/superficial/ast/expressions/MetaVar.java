package poulet.superficial.ast.expressions;

public class MetaVar extends Expression.Projectable {
    public final Symbol symbol;

    public MetaVar(Symbol symbol) {
        this.symbol = symbol;
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return "?" + symbol;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MetaVar) {
            MetaVar other = (MetaVar) obj;
            return symbol.equals(other.symbol);
        }

        return false;
    }

    @Override
    public boolean occurs(Symbol symbol) {
        return false;
    }

    @Override
    public poulet.kernel.ast.MetaVar project() {
        return new poulet.kernel.ast.MetaVar(symbol.project());
    }
}
