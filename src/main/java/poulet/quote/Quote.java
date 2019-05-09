package poulet.quote;

import poulet.value.Name;

public class Quote implements Name {
    public final int level;

    public Quote(int level) {
        this.level = level;
    }

    @Override
    public Quote increment() {
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
        return "" + level;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Quote) {
            Quote quote = (Quote) obj;
            return quote.level == this.level;
        }
        return false;
    }
}
