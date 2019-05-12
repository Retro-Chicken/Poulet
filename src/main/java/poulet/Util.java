package poulet;

import poulet.ast.Symbol;

public class Util {
    private static int UNIQUE_TAG = 0;

    public static Symbol getUniqueSymbol() {
        UNIQUE_TAG++;
        return new Symbol("!" + UNIQUE_TAG);
    }
}
