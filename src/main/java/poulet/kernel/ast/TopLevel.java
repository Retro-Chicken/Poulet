package poulet.kernel.ast;

import poulet.parser.KernelAST;

public abstract class TopLevel extends KernelAST {
    public abstract TopLevel makeSymbolsUnique();
}
