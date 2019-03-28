package poulet;

import org.antlr.v4.runtime.CharStreams;
import poulet.parser.ASTParser;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        String parsed = ASTParser.parse(CharStreams.fromFileName("test/simple_program.poulet")).toString();
        System.out.println(parsed);
    }

    public static boolean test() {
        return true;
    }
}
