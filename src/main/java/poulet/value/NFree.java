package poulet.value;

public class NFree extends Neutral {
    public final Name symbol;

    public NFree(Name symbol) {
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return "" + symbol;
    }
}
