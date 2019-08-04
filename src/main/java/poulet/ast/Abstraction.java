package poulet.ast;

public class Abstraction extends Expression {
    public Symbol argumentSymbol;
    public Expression argumentType;
    public Expression body;

    public Abstraction(Symbol argumentSymbol, Expression argumentType, Expression body) {
        this.argumentSymbol = argumentSymbol;
        this.argumentType = argumentType;
        this.body = body;
    }

    public Abstraction(Symbol argumentSymbol, Expression body) {
        this.argumentSymbol = argumentSymbol;
        this.argumentType = null;
        this.body = body;
    }

    @Override
    public poulet.kernel.ast.Expression compile() {
        return new poulet.kernel.ast.Abstraction(
                argumentSymbol.compile(),
                argumentType == null ? new poulet.kernel.ast.MetaVar() : argumentType.compile(),
                body.compile()
        );
    }
}
