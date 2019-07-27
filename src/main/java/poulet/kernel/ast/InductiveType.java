package poulet.kernel.ast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class InductiveType extends Expression {
    public final Symbol inductiveType;
    public final List<Expression> parameters;
    public final List<Expression> arguments;

    public InductiveType(Symbol inductiveType, List<Expression> parameters, List<Expression> arguments) {
        this.inductiveType = inductiveType;
        this.parameters = parameters;
        this.arguments = arguments;
    }

    public InductiveType(InductiveType inductiveType) {
        this(
                inductiveType.inductiveType,
                new ArrayList<>(inductiveType.parameters),
                new ArrayList<>(inductiveType.arguments)
        );
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public InductiveType transformVars(Function<Var, Expression> transformation) {
        List<Expression> newParameters = new ArrayList<>();

        for (Expression parameter : parameters) {
            newParameters.add(parameter.transformVars(transformation));
        }

        List<Expression> newArguments = new ArrayList<>();

        for (Expression argument : arguments) {
            newArguments.add(argument.transformVars(transformation));
        }
        return new InductiveType(
                inductiveType,
                newParameters,
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

        return false;
    }

    @Override
    public String toString() {
        String s = "" + inductiveType + '[';
        s += parameters.stream().map(Expression::toString).collect(Collectors.joining(", "));
        s += ']';
        s += '[';
        s += arguments.stream().map(Expression::toString).collect(Collectors.joining(", "));
        s += ']';
        return s;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof InductiveType) {
            InductiveType other = (InductiveType) obj;
            return inductiveType.equals(other.inductiveType) &&
                    parameters.equals(other.parameters) &&
                    arguments.equals(other.arguments);
        }

        return false;
    }

    @Override
    InductiveType transformSymbols(Function<Symbol, Symbol> transformer, Map<Symbol, Symbol> unique) {
        List<Expression> newParameters = new ArrayList<>();

        for (Expression parameter : parameters) {
            newParameters.add(parameter.transformSymbols(transformer, unique));
        }

        List<Expression> newArguments = new ArrayList<>();

        for (Expression argument : arguments) {
            newArguments.add(argument.transformSymbols(transformer, unique));
        }

        return new InductiveType(
                inductiveType,
                newParameters,
                newArguments
        );
    }
}
