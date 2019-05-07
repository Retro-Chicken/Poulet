package poulet;

import org.antlr.v4.runtime.CharStreams;
import poulet.ast.Program;
import poulet.interpreter.Interpreter;
import poulet.parser.ASTParser;

import java.io.PrintWriter;

public class Main {
    public static void main(String[] args) throws Exception {
        Program program = ASTParser.parse(CharStreams.fromFileName("test/scratch.poulet"));
        PrintWriter printWriter = new PrintWriter(System.out);
        Interpreter.run(program, printWriter);
        printWriter.flush();
        printWriter.close();
    }

    public static boolean test() {
        return true;
    }
}
