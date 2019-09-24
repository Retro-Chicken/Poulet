package editor.poulet;

import org.antlr.v4.runtime.CharStreams;
import poulet.parser.SuperficialASTParser;
import poulet.superficial.Superficial;

import java.io.PrintWriter;

public class PouletRunner {
    public static void runString(String text, PrintWriter out) {
        try {
            poulet.superficial.ast.Program sugarProgram = SuperficialASTParser.parse(CharStreams.fromString(text));
            Superficial superficial = new Superficial(out);
            superficial.runProgram(sugarProgram);
        } catch(Exception e) {
            out.println(e.toString());
        }
    }
}
