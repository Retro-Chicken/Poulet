package poulet.ast;

import poulet.exceptions.PouletException;
import poulet.typing.Environment;
import poulet.util.ExpressionVisitor;

public class Type extends Sort {
    public final int level;

    public Type(int level, Environment environment) {
        super(environment);
        this.level = level;
    }

    public Type(int level) {
        this(level, null);
    }

    @Override
    public String toString() {
        return "Type" + level;
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) throws PouletException {
        return visitor.visit(this);
    }

    public Type context(Environment environment) {
        return new Type(level, environment);
    }
}