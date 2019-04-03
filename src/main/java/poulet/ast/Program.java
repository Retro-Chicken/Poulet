package poulet.ast;

import java.util.List;
import java.util.stream.Collectors;

public class Program extends Node {
    public List<TopLevel> program;

    public Program(List<TopLevel> program) {
        this.program = program;
    }

    public Program(Program program) { this.program = List.copyOf(program.program); }

    @Override
    public String toString() {
        return program.stream()
                .map(Object::toString)
                .collect(Collectors.joining("\n"));
    }
}
