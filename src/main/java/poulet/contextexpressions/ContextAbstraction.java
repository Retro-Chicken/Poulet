package poulet.contextexpressions;

import poulet.ast.Abstraction;
import poulet.ast.Symbol;
import poulet.exceptions.PouletException;
import poulet.typing.Environment;
import poulet.util.ContextExpressionVisitor;

public class ContextAbstraction extends ContextExpression {
    public final Symbol symbol;
    public final ContextExpression type;
    public final ContextExpression body;

    public ContextAbstraction(Abstraction abstraction, Environment environment) throws PouletException {
        super(abstraction, environment);
        this.symbol = abstraction.symbol;
        this.type = abstraction.type.contextExpression(environment);
        this.body = abstraction.body.contextExpression(environment.appendType(abstraction.symbol, abstraction.type));
    }

    public <T> T accept(ContextExpressionVisitor<T> visitor) throws PouletException {
        return visitor.visit(this);
    }
}
