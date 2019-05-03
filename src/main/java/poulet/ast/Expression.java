package poulet.ast;

public abstract class Expression extends Node {
    abstract public Expression transform(String offset);
}
