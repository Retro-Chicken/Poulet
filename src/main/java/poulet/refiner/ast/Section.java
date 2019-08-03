package poulet.refiner.ast;

import poulet.kernel.ast.TopLevel;
import poulet.util.StringUtil;

import java.util.List;

public class Section extends Sugar {
    public final String name;
    public final Program program;

    public Section(String name, Program program) {
        this.name = name;
        this.program = program;
    }

    @Override
    public List<TopLevel> inflate() {
        return program.project().topLevels;
    }

    @Override
    public String toString() {
        return "Section " + name + " {\n" + StringUtil.indent(program.toString(), 2) + "\n}";
    }
}
