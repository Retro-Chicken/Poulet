package poulet.superficial.ast.expressions;

import poulet.superficial.ast.inlines.Inline;

public abstract class TopLevel extends Inline.Projectable {
    public abstract poulet.kernel.ast.TopLevel project();
    public abstract TopLevel makeSymbolsUnique();
}
