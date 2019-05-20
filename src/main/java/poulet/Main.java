package poulet;

import org.antlr.v4.runtime.CharStreams;
import poulet.ast.*;
import poulet.interpreter.ImportHandler;
import poulet.interpreter.Interpreter;
import poulet.parser.ASTParser;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class Main {

    public static void main(String[] args) throws Exception {
        assert args.length >= 1;

        // Get imports
        String fileName = new File(args[0]).getCanonicalPath();
        Map<File, Boolean> directories = new HashMap<>();
        boolean recursive = false;
        for(int i = 1; i < args.length; i++)
            if(args[i].equals("-r"))
                recursive = true;
            else
                directories.put(new File(args[i]), recursive);

        Program program = ASTParser.parse(CharStreams.fromFileName(fileName));
        program = ImportHandler.includeImports(program, directories, fileName);

        PrintWriter printWriter = new PrintWriter(System.out);
        Interpreter.run(program, printWriter);
        printWriter.flush();
        printWriter.close();
    }

    public static boolean test() {
        return true;
    }
}
