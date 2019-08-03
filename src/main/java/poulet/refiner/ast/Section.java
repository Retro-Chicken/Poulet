package poulet.refiner.ast;

import poulet.parser.Node;
import poulet.util.StringUtil;

public class Section extends Node {
    public final String name;
    public final Program program;

    public Section(String name, Program program) {
        this.name = name;
        this.program = program;
    }

    @Override
    public String toString() {
        return "Section " + name + " {\n" + StringUtil.indent(program.toString(), 2) + "\n}";
    }
}
