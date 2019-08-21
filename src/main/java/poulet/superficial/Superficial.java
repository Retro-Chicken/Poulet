package poulet.superficial;

import poulet.kernel.Kernel;
import poulet.superficial.ast.Program;

public class Superficial {
    public void runProgram(Program program) {
        Kernel kernel = new Kernel();
        kernel.runProgram(program.project());
    }
}
