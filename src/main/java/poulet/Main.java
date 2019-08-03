package poulet;

import org.antlr.v4.runtime.CharStreams;
import poulet.kernel.Kernel;
import poulet.kernel.ast.*;
import poulet.parser.KernelASTParser;
import poulet.parser.RefinerASTParser;
import poulet.refiner.Refiner;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        /*
        Program program = KernelASTParser.parse(CharStreams.fromFileName("samples/hott/chapter1.poulet"));
        Kernel kernel = new Kernel();
        kernel.runProgram(program);
         */
        poulet.refiner.ast.Program program = RefinerASTParser.parse(CharStreams.fromFileName("samples/hott/chapter1.poulet"));
        Refiner refiner = new Refiner();
        refiner.runProgram(program);
    }
}
