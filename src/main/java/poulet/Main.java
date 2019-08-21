package poulet;

import org.antlr.v4.runtime.CharStreams;
import poulet.kernel.Kernel;
import poulet.parser.KernelASTParser;
import poulet.parser.SugarASTParser;
import poulet.refiner.Refiner;
import poulet.refiner.imports.ImportHandler;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        args = new String[]{"samples/scratch.poulet", "samples/hott/chapter2.poulet"};

        System.out.println("Testing Kernel on " + args[0] + "...");
        poulet.kernel.ast.Program kernelProgram = KernelASTParser.parse(CharStreams.fromFileName(args[0]));
        Kernel kernel = new Kernel();
        kernel.runProgram(kernelProgram);

        System.out.println("Testing Sugar on " + args[1] + "...");
        ImportHandler.directories.add("samples/");
        poulet.refiner.ast.Program sugarProgram = SugarASTParser.parse(CharStreams.fromFileName(args[1]));
        Refiner refiner = new Refiner();
        refiner.runProgram(sugarProgram);
    }
}
