package poulet.superficial.ast;

import poulet.parser.SuperficialNode;
import poulet.util.StringUtil;

import java.util.List;

public class Section extends Multiline {
    public final String name;
    public final Program program;

    public Section(String name, Program program) {
        this.name = name;
        this.program = program;
    }

    @Override
    public List<SuperficialNode> inflate() {
        return program.nodes;
    }

    @Override
    public String toString() {
        return "Section " + name + " {\n" + StringUtil.indent(program.toString(), 2) + "\n}";
    }
}
