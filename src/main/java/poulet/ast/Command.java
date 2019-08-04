package poulet.ast;

import java.util.List;

public class Command extends TopLevel {
    public enum Action {
        REDUCE,
        DEDUCE,
        ASSERT
    }

    public Action action;
    public List<Expression> arguments;

    public Command(Action action, List<Expression> arguments) {
        this.action = action;
        this.arguments = arguments;
    }
}
