package poulet.ast;

public class Assert extends TopLevel {
    public final Expression a, b;

    public Assert(Expression a, Expression b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public String toString() {
        return "#assert " + a + " ~ " + b;
    }
}
