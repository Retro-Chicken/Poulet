package poulet.contextexpressions;

import poulet.ast.Variable;
import poulet.exceptions.PouletException;
import poulet.typing.Environment;
import poulet.util.ContextExpressionVisitor;

public class ContextVariable extends ContextExpression {
    public ContextVariable(Variable variable, Environment environment) {
        super(variable, environment);
    }

    public <T> T accept(ContextExpressionVisitor<T> visitor) throws PouletException {
        return visitor.visit(this);
    }
}
