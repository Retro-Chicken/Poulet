package poulet.refiner;

import poulet.kernel.Kernel;
import poulet.kernel.ast.TopLevel;
import poulet.parser.Node;
import poulet.refiner.ast.Program;

import java.util.ArrayList;
import java.util.List;

public class Refiner {
    public void runProgram(Program program) {
        poulet.kernel.ast.Program kernelProgram = toKernel(program);
        Kernel kernel = new Kernel();
        kernel.runProgram(kernelProgram);
    }

    private poulet.kernel.ast.Program toKernel(Program program) {
        List<TopLevel> topLevels = new ArrayList<>();
        for(Node node : program.nodes) {
            if(node instanceof TopLevel)
                topLevels.add((TopLevel) node);
        }
        return new poulet.kernel.ast.Program(topLevels);
    }
}
