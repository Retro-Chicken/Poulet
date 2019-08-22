package poulet.superficial.ast.expressions;

import poulet.superficial.Desugar;

import java.util.List;
import java.util.stream.Collectors;

public class Command extends TopLevel {
    public enum Action {
        REDUCE,
        DEDUCE,
        ASSERT
    }

    public final Action action;
    public final List<Expression> arguments;

    public Command(Action action, List<Expression> arguments) {
        this.action = action;
        this.arguments = arguments;
    }

    @Override
    public String toString() {
        String argumentString;
        switch (action) {
            case ASSERT:
                argumentString = arguments.stream().map(Expression::toString).collect(Collectors.joining(" ~ "));
                break;
            default:
                argumentString = arguments.stream().map(Expression::toString).collect(Collectors.joining(" "));
        }
        return "#" + action.toString().toLowerCase() + " " + argumentString;
    }

    @Override
    public poulet.kernel.ast.Command project() {
        poulet.kernel.ast.Command.Action action;
        switch (this.action) {
            case REDUCE:
                action = poulet.kernel.ast.Command.Action.REDUCE;
                break;
            case DEDUCE:
                action = poulet.kernel.ast.Command.Action.DEDUCE;
                break;
            case ASSERT:
                action = poulet.kernel.ast.Command.Action.ASSERT;
                break;
            default:
                action = null;
                break;
        }
        return new poulet.kernel.ast.Command(action,
                arguments.stream().map(Desugar::desugar).collect(Collectors.toList()));
    }
}
