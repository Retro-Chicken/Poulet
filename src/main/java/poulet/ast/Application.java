package poulet.ast;

public class Application extends Expression {
    Expression function;
    Expression argument;

    public Application(Expression function, Expression argument) {
        this.function = function;
        this.argument = argument;
    }

    @Override
    public String toString() {
        return String.format("(%s) %s", function, argument);
    }
}
