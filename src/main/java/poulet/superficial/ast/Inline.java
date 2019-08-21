package poulet.superficial.ast;

import poulet.parser.KernelAST;

public abstract class Inline extends Sugar {
    public static abstract class Transformable extends Inline {
        public abstract Inline transform();
    }
    public static abstract class Projectable extends Inline {
        public abstract KernelAST project();
    }
}
