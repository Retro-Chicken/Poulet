package poulet.ast;

public class PiType extends Expression {
    public final Symbol variable;
    public final Expression type;
    public final Expression body;

    public PiType(Symbol variable, Expression type, Expression body) {
        this.variable = variable;
        this.type = type;
        this.body = body;
    }

    @Override
    public String toString() {
        return String.format("{%s : %s} %s", variable, type, body);
    }
    /*
    public PiType transform(String offset) {
        return new PiType(variable.transform(offset), type.transform(offset), body.transform(offset));
    }
     */

    public PiType offset(int offset) {
        return new PiType(variable, type.offset(offset), body.offset(offset));
    }
}
