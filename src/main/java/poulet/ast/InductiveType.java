package poulet.ast;

import poulet.exceptions.PouletException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InductiveType extends Expression {
    public final Symbol type;
    private final boolean concrete;
    public final List<Expression> parameters;
    public final List<Expression> arguments;

    public InductiveType(Symbol type, boolean concrete, List<Expression> parameters) {
        this.type = type;
        this.concrete = concrete;
        this.parameters = parameters;
        this.arguments = concrete ? new ArrayList<>() : null;
    }

    public InductiveType(Symbol type, boolean concrete, List<Expression> parameters, List<Expression> arguments) {
        if (concrete && arguments == null) {
            System.out.println(type);
            System.out.println(parameters);
            System.out.println(">>>the fuck");
        }

        this.type = type;
        this.concrete = concrete;
        this.parameters = parameters;
        this.arguments = arguments;
    }

    public boolean isConcrete() {
        return concrete;
    }

    @Override
    public String toString() {
        String s = "" + type + '[';
        s += parameters.stream().map(Expression::toString).collect(Collectors.joining(", "));
        s += ']';

        if (isConcrete() && !arguments.isEmpty()) {
            s += '(';
            s += arguments.stream().map(Expression::toString).collect(Collectors.joining(", "));
            s += ')';
        }

        return s;
    }

    @Override
    InductiveType makeSymbolsUnique(Map<Symbol, Symbol> map) throws PouletException {
        List<Expression> newParameters = new ArrayList<>();

        for (Expression parameter : parameters) {
            newParameters.add(parameter.makeSymbolsUnique(map));
        }

        List<Expression> newArguments = null;
        if (isConcrete()) {
            newArguments = new ArrayList<>();

            for (Expression argument : arguments) {
                newArguments.add(argument.makeSymbolsUnique(map));
            }
        }

        return new InductiveType(
                type,
                concrete,
                newParameters,
                newArguments
        );
    }

    public <T> T accept(ExpressionVisitor<T> visitor) throws PouletException {
        return visitor.visit(this);
    }
}
