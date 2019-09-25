package poulet.superficial.ast.multilines;

import poulet.parser.SuperficialNode;

import java.util.List;

public abstract class Multiline extends SuperficialNode {
    public abstract List<SuperficialNode> inflate();
}
