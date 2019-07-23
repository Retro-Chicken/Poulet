package poulet.kernel.ast;

import java.util.ArrayList;
import java.util.List;

public class Program extends Node {
    public final List<TopLevel> topLevels;

    public Program(List<TopLevel> topLevels) {
        this.topLevels = topLevels;
    }

    public Program makeSymbolsUnique() {
        List<TopLevel> unique = new ArrayList<>();

        for (TopLevel topLevel : topLevels) {
            unique.add(topLevel.makeSymbolsUnique());
        }

        return new Program(unique);
    }
}
