package poulet.ast;

import poulet.exceptions.PouletException;
import poulet.util.TopLevelVisitor;

import java.util.Map;

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

    @Override
    Print makeSymbolsUnique(Map<Symbol, Symbol> map) throws PouletException {
        return new Print(
                command,
                expression.makeSymbolsUnique()
        );
    }

    @Override
    public <T> T accept(TopLevelVisitor<T> visitor) throws PouletException {
        return visitor.visit(this);
    }
}
