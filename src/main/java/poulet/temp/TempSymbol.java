package poulet.temp;

import poulet.value.Name;

public class TempSymbol implements Name {
    public int level;

    public TempSymbol(int level) {
        this.level = level;
    }

    @Override
    public TempSymbol increment() {
        return this;
    }

    @Override
    public boolean isFree() {
        return true;
    }

    @Override
    public Integer getIndex() {
        return null;
    }

    @Override
    public String getName() {
        return "TEMP" + level;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof TempSymbol) {
            TempSymbol tempSymbol = (TempSymbol) obj;
            return tempSymbol.level == this.level;
        }
        return false;
    }

    @Override
    public String toString() {
        return getName();
    }
}
