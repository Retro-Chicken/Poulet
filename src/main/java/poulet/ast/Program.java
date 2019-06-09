package poulet.ast;

import poulet.exceptions.PouletException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Program extends Node {
    public final List<TopLevel> program;

    public Program(List<TopLevel> program) {
        this.program = program;
    }

    public Program(Program program) { this.program = new ArrayList<>(program.program); }

    @Override
    public String toString() {
        return program.stream()
                .map(Object::toString)
                .collect(Collectors.joining("\n"));
    }

    public Program makeSymbolsUnique() throws PouletException {
        List<TopLevel> newProgram = new ArrayList<>();

        for (TopLevel topLevel : program) {
            newProgram.add(topLevel.makeSymbolsUnique());
        }

        return new Program(newProgram);
    }
}
