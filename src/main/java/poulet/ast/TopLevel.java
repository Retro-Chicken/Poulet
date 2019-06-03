package poulet.ast;

import poulet.exceptions.PouletException;
import poulet.util.TopLevelVisitor;

import java.util.HashMap;
import java.util.Map;

public abstract class TopLevel extends Node {
    public TopLevel makeSymbolsUnique() throws PouletException {
        return makeSymbolsUnique(new HashMap<>());
    }

    abstract TopLevel makeSymbolsUnique(Map<Symbol, Symbol> map) throws PouletException;

    public abstract <T> T accept(TopLevelVisitor<T> visitor) throws PouletException;
}
