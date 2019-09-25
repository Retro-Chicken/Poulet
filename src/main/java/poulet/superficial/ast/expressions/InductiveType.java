package poulet.superficial.ast.expressions;

import poulet.superficial.Desugar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class InductiveType extends Expression.Projectable {
    public final Symbol inductiveType;
    public final List<Expression> parameters;
    public final List<Expression> arguments;

    public InductiveType(Symbol inductiveType, List<Expression> parameters, List<Expression> arguments) {
        this.inductiveType = inductiveType;
        this.parameters = parameters;
        this.arguments = arguments;
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
    public InductiveType transformSymbols(Function<Symbol, Symbol> transformer, Map<Symbol, Symbol> unique) {
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

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        String parametersString = parameters.stream().map(Expression::toString).collect(Collectors.joining(", "));
        String argumentsString = arguments.stream().map(Expression::toString).collect(Collectors.joining(", "));
        String s = "" + inductiveType;
        s += '[' + parametersString + ']';
        s += argumentsString.isEmpty() ? "" : '(' + argumentsString + ')';
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
    public boolean occurs(Symbol symbol) {
        for (Expression parameter : parameters) {
            if (parameter.occurs(symbol)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public poulet.kernel.ast.InductiveType project() {
        return new poulet.kernel.ast.InductiveType(
                inductiveType.project(),
                parameters.stream().map(Desugar::desugar).collect(Collectors.toList()),
                arguments.stream().map(Desugar::desugar).collect(Collectors.toList())
        );
    }
}
