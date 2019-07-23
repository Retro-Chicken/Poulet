package poulet.kernel.ast;

public abstract class TopLevel extends Node {
    public abstract TopLevel makeSymbolsUnique();
}
