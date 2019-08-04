package poulet.ast;

public class Application extends Expression {
    public Expression function;
    public Expression argument;

    public Application(Expression function, Expression argument) {
        this.function = function;
        this.argument = argument;
    }

    @Override
    public poulet.kernel.ast.Expression compile() {
        return new poulet.kernel.ast.Application(
                function.compile(),
                argument.compile()
        );
    }
}
