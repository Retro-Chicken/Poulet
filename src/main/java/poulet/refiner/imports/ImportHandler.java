package poulet.refiner.imports;

import org.antlr.v4.runtime.CharStreams;
import poulet.PouletException;
import poulet.parser.RefinerASTParser;
import poulet.refiner.ast.Import;
import poulet.refiner.ast.Program;
import poulet.refiner.ast.Section;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ImportHandler {
    public static List<String> directories = new ArrayList<>();

    public static Program expand(Import include) {
        String fileName = include.importName + ".poulet";
        String pathResult = FileSearcher.findFile(fileName, directories);
        if(pathResult == null)
            try {
                List<String> linkedLibraries = new ArrayList<>();
                for(String dir : ImportHandler.directories)
                    linkedLibraries.add(new File(dir).getCanonicalPath());
                throw new PouletException("import file " + fileName + " not found in linked libraries "
                        + linkedLibraries.stream().collect(Collectors.joining(", ")));
            } catch (IOException exception) {
                throw new PouletException("import file " + fileName + " not found in linked libraries");
            }

        try {
            Program program = RefinerASTParser.parse(CharStreams.fromFileName(pathResult));
            for(int i = 0; i < include.subSections.size(); i++) {
                Section section = program.getSection(include.subSections.get(i));
                if(section == null)
                    throw new PouletException("subsection " + include.importName + "." + include.subSections.subList(0, i + 1).stream().collect(Collectors.joining(".")) + " not found");
                program = section.program;
            }
            return program;
        } catch(IOException exception) {
            throw new PouletException("error importing file");
        }
    }
}
