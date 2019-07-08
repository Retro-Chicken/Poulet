package poulet.contextexpressions;

import poulet.ast.Prop;
import poulet.exceptions.PouletException;
import poulet.typing.Environment;
import poulet.util.ContextExpressionVisitor;

public class ContextProp extends ContextExpression {
    public ContextProp(Prop prop, Environment environment) throws PouletException {
        super(prop, environment);
    }

    public ContextProp(Environment environment) throws PouletException {
        this(new Prop(), environment);
    }

    public <T> T accept(ContextExpressionVisitor<T> visitor) throws PouletException {
        return visitor.visit(this);
    }
}
