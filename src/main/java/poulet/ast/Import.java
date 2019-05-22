package poulet.ast;

import poulet.exceptions.PouletException;

import java.util.Map;

public class Import extends TopLevel {
    public String fileName;

    public Import(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String toString() {
        return "#import \"" + fileName + "\"";
    }

    @Override
    Import makeSymbolsUnique(Map<Symbol, Symbol> map) throws PouletException {
        return this;
    }

    @Override
    public <T> T accept(TopLevelVisitor<T> visitor) throws PouletException {
        return visitor.visit(this);
    }
}
