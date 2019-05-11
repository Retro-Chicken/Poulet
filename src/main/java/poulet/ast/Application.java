package poulet.ast;

public class Application extends Expression {
    public final Expression function;
    public final Expression argument;

    public Application(Expression function, Expression argument) {
        this.function = function;
        this.argument = argument;
    }

    @Override
    public String toString() {
        return String.format("(%s) %s", function, argument);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Application) {
            Application other = (Application) obj;
            return function.equals(other.function) && argument.equals(other.argument);
        }

        return false;
    }
    /*
    public Application transform(String offset) {
        return new Application(function.transform(offset), argument.transform(offset));
    }
     */
    public Application offset(int offset) {
        return new Application(function.offset(offset), argument.offset(offset));
    }
}
