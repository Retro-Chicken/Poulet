package poulet.superficial.ast.expressions;

import java.util.List;
import java.util.stream.Collectors;

public class ConstructorCall extends Expression {
    public final Symbol inductiveType;
    public final List<Expression> parameters;
    public final Symbol constructor;
    public final List<Expression> arguments;

    public ConstructorCall(Symbol inductiveType, List<Expression> parameters, Symbol constructor, List<Expression> arguments) {
        this.inductiveType = inductiveType;
        this.parameters = parameters;
        this.constructor = constructor;
        this.arguments = arguments;
    }

    @Override
    public String toString() {
        String parametersString = parameters.stream().map(Expression::toString).collect(Collectors.joining(", "));
        String argumentsString = arguments.stream().map(Expression::toString).collect(Collectors.joining(", "));
        return "" + inductiveType + "[" + parametersString + "]" + "." + constructor
                    + (argumentsString.isEmpty() ? "" : "(" + argumentsString + ")");
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ConstructorCall) {
            ConstructorCall other = (ConstructorCall) obj;
            return inductiveType.equals(other.inductiveType) &&
                    parameters.equals(other.parameters) &&
                    constructor.equals(other.constructor) &&
                    arguments.equals(other.arguments);
        }

        return false;
    }

    @Override
    public boolean occurs(Symbol symbol) {
        for (Expression parameter : parameters) {
            if (parameter.occurs(symbol)) {
                return true;
            }
        }

        for (Expression argument : arguments) {
            if (argument.occurs(symbol)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public poulet.kernel.ast.ConstructorCall project() {
        return new poulet.kernel.ast.ConstructorCall(
                inductiveType.project(),
                parameters.stream().map(Expression::project).collect(Collectors.toList()),
                constructor.project(),
                arguments.stream().map(Expression::project).collect(Collectors.toList())
        );
    }
}
