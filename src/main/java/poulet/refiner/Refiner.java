package poulet.refiner;

import poulet.kernel.Kernel;
import poulet.refiner.ast.Program;

public class Refiner {
    public void runProgram(Program program) {
        Kernel kernel = new Kernel();
        kernel.runProgram(program.project());
    }
}
