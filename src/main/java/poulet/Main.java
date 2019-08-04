package poulet;

import org.antlr.v4.runtime.CharStreams;
import poulet.intepreter.Interpreter;
import poulet.parser.VernacularASTParser;
import poulet.ast.Program;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        //Program program = KernelASTParser.parse(CharStreams.fromFileName("samples/hott/chapter1.poulet"));
        //Kernel kernel = new Kernel();
        //kernel.runProgram(program);
        Program program = VernacularASTParser.parse(CharStreams.fromFileName("samples/vernacular.poulet"));
        Interpreter interpreter = new Interpreter();
        interpreter.run(program);
    }
}
