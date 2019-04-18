package poulet.ast;

public class Print extends TopLevel {
    public enum PrintCommand {
        reduce,
        check,
        scholiums
    }

    public final PrintCommand command;
    public final Expression expression;

    public Print(PrintCommand command, Expression expression) {
        this.command = command;
        this.expression = expression;
    }

    @Override
    public String toString() {
        return String.format("#%s %s", command.toString(), expression.toString());
    }
}
