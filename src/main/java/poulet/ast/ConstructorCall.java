package poulet.ast;

public class ConstructorCall extends Expression {
    private InductiveType inductiveType;
    private Symbol constructor;

    public ConstructorCall(InductiveType inductiveType, Symbol constructor) {
        this.inductiveType = inductiveType;
        this.constructor = constructor;
    }

    @Override
    public String toString() {
        return inductiveType.toString() + '.' + constructor;
    }
}
