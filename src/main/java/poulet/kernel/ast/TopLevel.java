package poulet.kernel.ast;

import poulet.parser.KernelNode;

public abstract class TopLevel extends KernelNode {
    public abstract TopLevel makeSymbolsUnique();
}
