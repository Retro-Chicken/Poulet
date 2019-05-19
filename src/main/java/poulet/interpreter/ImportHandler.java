package poulet.interpreter;

import org.antlr.v4.runtime.CharStreams;
import poulet.ast.Import;
import poulet.ast.Print;
import poulet.ast.Program;
import poulet.ast.TopLevel;
import poulet.parser.ASTParser;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImportHandler {
    public static Program includeImports(Program program, Map<File, Boolean> directories, String fileName) throws Exception {
        Program result = new Program(program);
        directories.put(new File(fileName).getParentFile(), true);
        List<String> imported = new ArrayList<>();
        imported.add(fileName);
        Map<String, Program> imports = getImports(program, directories, imported);
        String simpleName = new File(fileName).getName();
        imports.put(simpleName, new Program(new ArrayList<>()));

        List<String> existingImports = new ArrayList<>();
        while(true) {
            boolean done = true;
            List<TopLevel> modifiedProgram = new ArrayList<>();
            for(TopLevel topLevel : result.program) {
                if(topLevel instanceof Import) {
                    Import importStatement = (Import) topLevel;
                    String importName = importStatement.fileName;
                    if(existingImports.contains(importName))
                        continue;
                    if(!imports.containsKey(importName))
                        throw new Exception("Invalid Import");
                    existingImports.add(importName);
                    modifiedProgram.addAll(imports.get(importName).program);
                    done = false;
                } else {
                    modifiedProgram.add(topLevel);
                }
            }
            result = new Program(modifiedProgram);
            if(done)
                break;
        }
        return result;
    }

    private static Map<String, Program> getImports(Program program, Map<File, Boolean> directories, List<String> imported) throws Exception {
        Map<String, Program> imports = new HashMap<>();
        for(TopLevel topLevel : program.program) {
            if(topLevel instanceof Import) {
                String name = ((Import) topLevel).fileName;
                for(File dir : directories.keySet()) {
                    List<String> results = search(dir, name, directories.get(dir));
                    if(results.size() == 0)
                        throw new Exception("Import does not exist");
                    else if(results.size() > 1)
                        throw new Exception("Multiple possible imports");
                    String result = results.get(0);
                    if(!imported.contains(result)) {
                        List<TopLevel> importedTops = ASTParser.parse(CharStreams.fromFileName(result)).program;
                        List<TopLevel> keep = new ArrayList<>();
                        for(TopLevel top : importedTops)
                            if(!(top instanceof Print || top instanceof Print))
                                keep.add(top);
                        imported.add(result);
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

    private static List<String> search(File directory, String name, boolean recursive) throws Exception {
        if(!directory.isDirectory())
            throw new Exception("Linked library is not a directory");

        List<String> result = new ArrayList<>();
        if(directory.canRead())
            for (File temp : directory.listFiles())
                if (temp.isDirectory() && recursive)
                    search(temp, name, recursive);
                else
                    if(name.equals(temp.getName()))
                        result.add(temp.getCanonicalPath());
        else
            throw new Exception("Linked library does not have read permission");
        return result;
    }
}
