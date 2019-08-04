package poulet.ast;

public class Prod extends Expression {
    public Symbol argumentSymbol;
    public Expression argumentType;
    public Expression bodyType;

    public Prod(Expression argumentType, Expression bodyType) {
        this.argumentSymbol = new Symbol("_");
        this.argumentType = argumentType;
        this.bodyType = bodyType;
    }

    public Prod(Symbol argumentSymbol, Expression argumentType, Expression bodyType) {
        this.argumentSymbol = argumentSymbol;
        this.argumentType = argumentType;
        this.bodyType = bodyType;
    }

    @Override
    public poulet.kernel.ast.Expression compile() {
        return new poulet.kernel.ast.Prod(
                argumentSymbol.compile(),
                argumentType.compile(),
                bodyType.compile()
        );
    }
}
