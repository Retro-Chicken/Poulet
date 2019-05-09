package poulet.value;

public class NApplication extends Neutral {
    public final Neutral function;
    public final Value argument;

    public NApplication(Neutral function, Value argument) {
        this.function = function;
        this.argument = argument;
    }

}
