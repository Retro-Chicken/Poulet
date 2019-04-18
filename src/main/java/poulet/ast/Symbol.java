package poulet.ast;

public class Symbol extends Node {
    private static int nextId = 0;

    public final String name;
    private final Integer id;

    public Symbol(String name) {
        this.name = name;
        this.id = null;
    }

    public Symbol(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public Symbol bind() {
        Symbol bound = new Symbol(name, nextId);
        nextId++;
        return bound;
    }

    public Symbol copyID(Symbol symbol) {
        return new Symbol(symbol.name, symbol.id);
    }

    @Override
    public String toString() {
        if (id == null)
            return name;
        else
            return String.format("%s(%d)", name, id);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Symbol) {
            Symbol other = (Symbol) obj;
            if (id == null && other.id == null) {
                return name.equals(other.name);
            } else if (id != null && other.id != null) {
                return name.equals(other.name) && id.equals(other.id);
            } else {
                return false;
            }
        }

        return false;
    }

    public boolean isFree() {
        return id == null;
    }

    /*public boolean weakEquals(Object obj) {
        if (obj instanceof Symbol) {
            Symbol other = (Symbol) obj;
            return name.equals(other.name);
        }

        return false;
    }*/
}
