package poulet.ast;

public class Symbol extends Node {
    private final String name;
    private final Integer index;

    public Symbol(String name) {
        this.name = name;
        this.index = null;
    }

    public Symbol(int index) {
        this.name = null;
        this.index = index;
    }

    @Override
    public String toString() {
        if (index == null)
            return name;
        else
            return "" + index;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Symbol) {
            Symbol other = (Symbol) obj;
            if (index != null && other.index != null) {
                return index.equals(other.index);
            } else if (name != null && other.name != null) {
                return name.equals(other.name);
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        if (name != null)
            return 2 * name.hashCode();
        if (index != null)
            return 2 * index.hashCode() + 1;
        return 0;
    }

    /**
     * increment index if indexed, otherwise don't change
     */
    public Symbol increment() {
        if (index != null)
            return new Symbol(index + 1);
        return this;
    }

    public boolean isFree() {
        return index == null;
    }

    /*public boolean weakEquals(Object obj) {
        if (obj instanceof Symbol) {
            Symbol other = (Symbol) obj;
            return name.equals(other.name);
        }

        return false;
    }*/

    public Symbol transform(String offset) {
        if(name != null)
            return new Symbol(name + offset);
        return this;
    }
}
