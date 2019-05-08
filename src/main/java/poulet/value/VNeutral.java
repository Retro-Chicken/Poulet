package poulet.value;

public class VNeutral extends Value {
    public final Neutral neutral;

    public VNeutral(Neutral neutral) {
        this.neutral = neutral;
    }

    @Override
    public String toString() {
        return neutral.toString();
    }
}
