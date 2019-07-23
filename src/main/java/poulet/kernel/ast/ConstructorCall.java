package poulet.kernel.ast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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

    public ConstructorCall(ConstructorCall constructorCall) {
        this(constructorCall.inductiveType, constructorCall.parameters, constructorCall.constructor, constructorCall.arguments);
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public ConstructorCall transformVars(Function<Var, Expression> transformation) {
        List<Expression> newParameters = new ArrayList<>();

        for (Expression parameter : parameters) {
            newParameters.add(parameter.transformVars(transformation));
        }

        List<Expression> newArguments = new ArrayList<>();

        for (Expression argument : arguments) {
            newArguments.add(argument.transformVars(transformation));
        }

        return new ConstructorCall(
                inductiveType,
                newParameters,
                constructor,
                newArguments
        );
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
    public String toString() {
        String parametersString = parameters.stream().map(Expression::toString).collect(Collectors.joining(", "));
        String argumentsString = arguments.stream().map(Expression::toString).collect(Collectors.joining(", "));
        return "" + inductiveType + "[" + parametersString + "]." + constructor + "[" + argumentsString + "]";
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
    ConstructorCall transformSymbols(Function<Symbol, Symbol> transformer, Map<Symbol, Symbol> unique) {
        List<Expression> newParameters = new ArrayList<>();

        for (Expression parameter : parameters) {
            newParameters.add(parameter.transformSymbols(transformer, unique));
        }

        List<Expression> newArguments = new ArrayList<>();

        for (Expression argument : arguments) {
            newArguments.add(argument.transformSymbols(transformer, unique));
        }

        return new ConstructorCall(
                inductiveType,
                newParameters,
                constructor,
                newArguments
        );
    }
}
