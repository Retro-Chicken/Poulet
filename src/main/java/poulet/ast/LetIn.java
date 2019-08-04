package poulet.ast;

public class LetIn extends Expression {
    public Symbol name;
    public Expression type;
    public Expression definition;
    public Expression body;

    public LetIn(Symbol name, Expression type, Expression definition, Expression body) {
        this.name = name;
        this.type = type;
        this.definition = definition;
        this.body = body;
    }

    public LetIn(Symbol name, Expression definition, Expression body) {
        this(name, null, definition, body);
    }

    @Override
    public poulet.kernel.ast.Expression compile() {
        return new poulet.kernel.ast.Application(
                new Abstraction(
                     name,
                     type,
                     body
                ).compile(),
                definition.compile()
        );
    }
}
