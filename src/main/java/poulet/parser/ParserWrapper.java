package poulet.parser;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

public class ParserWrapper extends PouletBaseListener {
    public static void run() {
        PouletLexer lexer = new PouletLexer(CharStreams.fromString("test"));
        PouletParser parser = new PouletParser(new CommonTokenStream(lexer));
        ParseTreeWalker.DEFAULT.walk(new ParserWrapper(), parser.root());
    }

    @Override
    public void enterS(PouletParser.SContext ctx) {
        System.out.println("found s");
    }
}
