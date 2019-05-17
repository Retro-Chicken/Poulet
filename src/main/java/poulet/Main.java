package poulet;

import org.antlr.v4.runtime.CharStreams;
import poulet.ast.*;
import poulet.interpreter.Interpreter;
import poulet.parser.ASTParser;

import java.io.PrintWriter;
import java.util.*;

public class Main {

    public static void main(String[] args) throws Exception {
        /*
        System.out.println(Arrays.toString(args));
        Program program = ASTParser.parse(CharStreams.fromFileName("test/inductive_test.poulet"));
        PrintWriter printWriter = new PrintWriter(System.out);
        Interpreter.run(program, printWriter);
        printWriter.flush();
        printWriter.close();
         */
        assert args.length >= 1;
        String fileName = args[0];
        List<String> directories = new ArrayList<>();
        for(int i = 1; i < args.length; i++)
            directories.add(args[i]);
        Program program = ASTParser.parse(CharStreams.fromFileName(fileName));
        Map<String, Program> imports = new HashMap<>();
        for(TopLevel topLevel : program.program) {
            if(topLevel instanceof Import) {
                String name = ((Import) topLevel).fileName;
                String actualFile;
                for(String dir : directories) {
                    if(dir.matches(".*" + name)) {
                        Program importedProgram = ASTParser.parse(CharStreams.fromFileName(dir));
                        imports.put(name, importedProgram);
                        break;
                    }
                }
            }
        }
        PrintWriter printWriter = new PrintWriter(System.out);
        Interpreter.run(program, printWriter, imports);
        printWriter.flush();
        printWriter.close();
    }

    public static boolean test() {
        return true;
    }
}
