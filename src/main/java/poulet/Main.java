package poulet;

import org.antlr.v4.runtime.CharStreams;
import poulet.kernel.Kernel;
import poulet.kernel.ast.*;
import poulet.parser.KernelASTParser;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        Program program = KernelASTParser.parse(CharStreams.fromFileName("samples/scratch.poulet"));
        Kernel kernel = new Kernel();
        kernel.runProgram(program);
    }
}
