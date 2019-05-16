package poulet;

import poulet.ast.Symbol;

public class Util {
    public static final String NULL_ABSTRACTION_SYMBOL = "_";
    public static final String NULL_PITYPE_SYMBOL = "_";

    private static int UNIQUE_TAG = 0;

    public static Symbol getUniqueSymbol() {
        UNIQUE_TAG++;
        return new Symbol("!" + UNIQUE_TAG);
    }

    public static  Symbol getReadableSymbol() {
        UNIQUE_TAG++;
        return new Symbol("" + (char)('a' + UNIQUE_TAG % 26));
    }
}
