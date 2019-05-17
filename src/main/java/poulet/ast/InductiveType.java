package poulet.ast;

import java.util.List;
import java.util.stream.Collectors;

public class InductiveType extends Expression {
    public final Symbol type;
    public final List<Expression> parameters;
    public final List<Expression> arguments;

    public InductiveType(Symbol type, List<Expression> parameters) {
        this(type, parameters, null);
    }

    public InductiveType(Symbol type, List<Expression> parameters, List<Expression> arguments) {
        this.type = type;
        this.parameters = parameters;
        this.arguments = arguments;
    }

    public boolean isConcrete() {
        return this.arguments != null;
    }

    @Override
    public String toString() {
        String s = "" + type + '[';
        s += parameters.stream().map(Expression::toString).collect(Collectors.joining(", "));
        s += ']';

        if (isConcrete()) {
            s += '(';
            s += arguments.stream().map(Expression::toString).collect(Collectors.joining(", "));
            s += ')';
        }

        return s;
    }
}
