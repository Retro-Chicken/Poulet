package poulet.superficial.ast;

import poulet.parser.KernelNode;
import poulet.parser.SuperficialNode;

public abstract class Inline extends SuperficialNode {
    public static abstract class Transformable extends Inline {
        public abstract Inline transform();
    }
    public static abstract class Projectable extends Inline {
        public abstract KernelNode project();
    }
}
