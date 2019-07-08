package poulet.contextexpressions;

import poulet.ast.Symbol;
import poulet.ast.Variable;
import poulet.exceptions.PouletException;
import poulet.typing.Environment;
import poulet.util.ContextExpressionVisitor;

public class ContextVariable extends ContextExpression {
    public final Symbol symbol;

    public ContextVariable(Variable variable, Environment environment) {
        super(variable, environment);
        this.symbol = variable.symbol;
    }

    public <T> T accept(ContextExpressionVisitor<T> visitor) throws PouletException {
        return visitor.visit(this);
    }
}
