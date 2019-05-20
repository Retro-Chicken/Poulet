package poulet.ast;

public class Output extends TopLevel {
    public final String text;

    public Output(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return String.format("#print '%s'", text);
    }
}
