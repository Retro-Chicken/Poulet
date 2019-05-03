package test.poulet;

import org.antlr.v4.runtime.CharStreams;
import org.junit.jupiter.api.Test;
import poulet.ast.Program;
import poulet.interpreter.Interpreter;
import poulet.parser.ASTParser;

import java.io.PrintWriter;
import java.io.StringWriter;

public class TestRun {
    @Test
    void testRun1() throws Exception {
        Program program = ASTParser.parse(CharStreams.fromFileName("test/run_test.poulet"));
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        Interpreter.run(program, printWriter);
        String output = stringWriter.getBuffer().toString();
        System.out.print(output);
    }
}
