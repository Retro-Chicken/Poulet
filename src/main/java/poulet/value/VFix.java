package poulet.value;

import poulet.ast.Fix;

public class VFix extends Value {
    public final Fix fix;

    public VFix(Fix fix) {
        this.fix = fix;
    }
}
