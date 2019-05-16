package poulet.ast;

import java.util.List;
import java.util.stream.Collectors;

public class InductiveType extends Expression {
    private Symbol type;
    private List<Expression> parameters;

    public InductiveType(Symbol type, List<Expression> parameters) {
        this.type = type;
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        String s = "" + type + '[';
        s += parameters.stream().map(Expression::toString).collect(Collectors.joining(", "));
        s += ']';
        return s;
    }
}
