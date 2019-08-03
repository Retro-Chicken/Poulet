package poulet.refiner.ast;

import poulet.kernel.ast.TopLevel;
import poulet.parser.Node;

import java.util.List;

public abstract class Sugar extends Node {
    public abstract List<TopLevel> inflate();
}
