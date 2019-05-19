package poulet.ast;

public class Symbol extends Node {
    public static int nextId = 0;

    public final String name;
    final Integer id;

    public Symbol(String name) {
        this.name = name;
        this.id = null;
    }

    private Symbol(String name, Integer id) {
        this.name = name;
        this.id = id;
    }

    public Symbol makeUnique() {
        return new Symbol(name, nextId++);
    }

    public Symbol copy() {
        return new Symbol(name, id);
    }

    @Override
    public String toString() {
        if (id == null)
            return name;
        else
            return String.format("%s@%d", name, id);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Symbol) {
            Symbol other = (Symbol) obj;

            if (id == null && other.id == null) {
                return name.equals(other.name);
            } else if (id != null && other.id != null){
                return name.equals(other.name) && id.equals(other.id);
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}
