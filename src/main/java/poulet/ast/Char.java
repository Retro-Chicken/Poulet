package poulet.ast;

public class Char extends Expression {
    public final char c;

    public Char(char c) {
        this.c = c;
    }

    @Override
    public String toString() {
        return "'" + c + "'";
    }
}
