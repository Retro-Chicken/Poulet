package poulet.intepreter;

import poulet.ast.Definition;
import poulet.ast.Program;
import poulet.ast.TopLevel;
import poulet.kernel.Kernel;

public class Interpreter {
    private Kernel kernel = new Kernel();

    public void run(Program program) {
        for (TopLevel topLevel : program.topLevels) {
            if (topLevel instanceof Definition) {
                Definition definition = (Definition) topLevel;
                processDefinition(definition);
            } else if (topLevel instanceof Command) {

            }
        }
    }

    private void processDefinition(Definition definition) {
        if (definition.definition == null) {
            kernel.assume(
                    definition.name.compile(),
                    definition.type.compile()
            );
        } else {
            kernel.define(
                    definition.name.compile(),
                    definition.type.compile(),
                    definition.definition.compile()
            );
        }
    }

    private void processCommand(Command command) {
        // TODO
    }
}
