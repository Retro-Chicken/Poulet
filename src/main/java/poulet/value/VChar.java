package poulet.value;

import poulet.ast.Char;

public class VChar extends Value {
    public final Char c;

    public VChar(Char c) {
        this.c = c;
    }
}
