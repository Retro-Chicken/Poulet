package poulet.superficial.ast.expressions;

import poulet.superficial.ast.Inline;

public abstract class Expression extends Inline {
    public abstract boolean occurs(Symbol symbol);
    public static abstract class Transformable extends Expression {
        public abstract Expression transform();
    }
    public static abstract class Projectable extends Expression {
        public abstract poulet.kernel.ast.Expression project();
    }
}
