package poulet.ast;

public abstract class Expression extends Node {
    //abstract public Expression transform(String offset);
    public Expression increment() {
        return offset(1);
    }
    public Expression decrement() {
        return offset(-1);
    }
    abstract public Expression offset(int offset);
}
