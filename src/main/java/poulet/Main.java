package poulet;

import org.antlr.v4.runtime.CharStreams;
import poulet.ast.*;
import poulet.interpreter.Interpreter;
import poulet.parser.ASTParser;

import java.io.File;
import java.io.IOException;
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
        if(fileName.contains("/"))
            directories.add(fileName.substring(0, fileName.lastIndexOf("/") + 1));
        else
            directories.add("");
        for(int i = 1; i < args.length; i++)
            directories.add(args[i]);
        Program program = ASTParser.parse(CharStreams.fromFileName(fileName));
        List<String> imported = new ArrayList<>();
        imported.add(fileName);
        Map<String, Program> imports = getImports(program, directories, imported);
        String simpleName;
        if(fileName.contains("/"))
            simpleName = fileName.substring(fileName.lastIndexOf("/") + 1, fileName.length());
        else
            simpleName = fileName;
        imports.put(simpleName, new Program(new ArrayList<>()));
        PrintWriter printWriter = new PrintWriter(System.out);
        Interpreter.run(program, printWriter, imports);
        printWriter.flush();
        printWriter.close();
    }

    private static Map<String, Program> getImports(Program program, List<String> directories, List<String> imported) throws IOException {
        Map<String, Program> imports = new HashMap<>();
        for(TopLevel topLevel : program.program) {
            if(topLevel instanceof Import) {
                String name = ((Import) topLevel).fileName;
                for(String dir : directories) {
                    File check = new File(dir + name);
                    if(check.exists() && !imported.contains(dir + name)) {
                        List<TopLevel> importedTops = ASTParser.parse(CharStreams.fromFileName(dir + name)).program;
                        List<TopLevel> keep = new ArrayList<>();
                        for(TopLevel top : importedTops) {
                            if(!(top instanceof Print || top instanceof Print))
                                keep.add(top);
                        }
                        imported.add(dir + name);
                        Program importedProgram = new Program(keep);
                        imports.put(name, importedProgram);
                        imports.putAll(getImports(importedProgram, directories, imported));
                        break;
                    }
                }
            }
        }
        return imports;
    }

    public static boolean test() {
        return true;
    }
}
