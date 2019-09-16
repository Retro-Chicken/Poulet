package poulet.superficial;

import poulet.PouletException;
import poulet.kernel.Kernel;
import poulet.parser.SuperficialNode;
import poulet.superficial.ast.Program;
import poulet.superficial.ast.expressions.*;

public class Superficial {
    public void runProgram(Program program) {
        Kernel kernel = new Kernel();
        program = program.makeSymbolsUnique();
        program = program.inflate();

        for (SuperficialNode node : program.nodes) {
            try {
                if (node instanceof Command) {
                    runCommand((Command) node, kernel);
                } else if (node instanceof TopLevel) {
                    kernel.handleTopLevel(Desugar.desugar((TopLevel) node));
                }
            } catch (PouletException e) {
                e.printStackTrace();
                System.err.println("\n on line: " + node);
            }
        }
    }

    public void runCommand(Command command, Kernel kernel) {
        if (command.action == Command.Action.ASSERT) {
            Expression a = command.arguments.get(0);
            Expression b = command.arguments.get(1);

            if (!kernel.convertible(Desugar.desugar(a), Desugar.desugar(b))) {
                throw new PouletException("assertion " + a + " ~ " + b + " failed");
            }
        } else if (command.action == Command.Action.DEDUCE) {
            Expression term = command.arguments.get(0);
            System.out.println(term + " : " + kernel.reduce(kernel.deduceType(Desugar.desugar(term))));
        } else if (command.action == Command.Action.REDUCE) {
            Expression term = command.arguments.get(0);
            System.out.println(term + " ▹ " + kernel.reduce(Desugar.desugar(term)));
        }
    }
}
