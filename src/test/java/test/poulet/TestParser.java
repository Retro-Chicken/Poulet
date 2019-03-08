package test.poulet;

import org.antlr.v4.runtime.CharStreams;
import org.junit.jupiter.api.Test;
import poulet.parser.ASTParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestParser {
    @Test
    void testParseSimpleProgram() throws IOException {
        String expected = Files.readString(Paths.get("test/simple_program.poulet"));
        String actual = ASTParser.parse(CharStreams.fromFileName("test/simple_program.poulet")).toString();
        expected = expected.replaceAll("\\s", "");
        actual = actual.replaceAll("\\s", "");
        assertEquals(expected, actual);
    }
}
