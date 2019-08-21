package poulet.superficial.ast.expressions;

import poulet.superficial.ast.Inline;

public abstract class Expression extends Inline.Projectable {
    public abstract poulet.kernel.ast.Expression project();
    public abstract boolean occurs(Symbol symbol);
}
