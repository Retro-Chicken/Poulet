package test.poulet;

import org.antlr.v4.runtime.CharStreams;
import org.junit.jupiter.api.Test;
import poulet.parser.ASTParser;

import java.io.IOException;

public class TestParser {
    @Test
    void testParseSimpleProgram() throws IOException {
        ASTParser.parse(CharStreams.fromFileName("test/simple_program.poulet"));
    }
}
