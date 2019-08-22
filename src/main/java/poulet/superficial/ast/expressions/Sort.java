package poulet.superficial.ast.expressions;

public abstract class Sort extends Expression.Projectable {
    public boolean occurs(Symbol symbol) {
        return false;
    }
}
