package poulet;

import org.antlr.v4.runtime.CharStreams;
import poulet.kernel.Kernel;
import poulet.parser.KernelASTParser;
import poulet.parser.SuperficialASTParser;
import poulet.superficial.Superficial;
import poulet.superficial.imports.ImportHandler;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        args = new String[]{"samples/scratch.poulet", "samples/hott/chapter2.poulet"};

        System.out.println("Testing Kernel on " + args[0] + "...");
        poulet.kernel.ast.Program kernelProgram = KernelASTParser.parse(CharStreams.fromFileName(args[0]));
        Kernel kernel = new Kernel();
        kernel.runProgram(kernelProgram);

        System.out.println("Testing Superficial on " + args[1] + "...");
        ImportHandler.directories.add("samples/");
        poulet.superficial.ast.Program sugarProgram = SuperficialASTParser.parse(CharStreams.fromFileName(args[1]));
        Superficial superficial = new Superficial();
        superficial.runProgram(sugarProgram);
    }
}
