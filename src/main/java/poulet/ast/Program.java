package poulet.ast;

import java.util.List;

public class Program extends Node {
    public List<TopLevel> topLevels;

    public Program(List<TopLevel> topLevels) {
        this.topLevels = topLevels;
    }
}
