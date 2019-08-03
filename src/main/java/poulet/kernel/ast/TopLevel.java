package poulet.kernel.ast;

import poulet.parser.Node;

public abstract class TopLevel extends Node {
    public abstract TopLevel makeSymbolsUnique();
}
