package poulet.kernel.ast;

import java.util.ArrayList;
import java.util.HashMap;
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
    public TopLevel makeSymbolsUnique() {
        List<Expression> unique = new ArrayList<>();

        for (Expression argument : arguments) {
            unique.add(argument.makeSymbolsUnique());
        }

        return new Command(action, unique);
    }
}
