package poulet.ast;

public class Print extends TopLevel {
    enum PrintCommand {
        Reduce,
        Check,
        Scholiums
    }

    PrintCommand command;

    public Print(PrintCommand command) {
        this.command = command;
    }
}
