package poulet.superficial.ast.expressions;

public abstract class Sort extends Expression {
    public boolean occurs(Symbol symbol) {
        return false;
    }
}
