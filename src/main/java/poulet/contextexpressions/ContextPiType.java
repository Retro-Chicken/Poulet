package poulet.contextexpressions;

import poulet.ast.PiType;
import poulet.ast.Symbol;
import poulet.exceptions.PouletException;
import poulet.typing.Environment;
import poulet.util.ContextExpressionVisitor;

public class ContextPiType extends ContextExpression {
    public final Symbol variable;
    public final ContextExpression type;
    public final ContextExpression body;

    public ContextPiType(PiType piType, Environment environment) throws PouletException {
        super(piType, environment);
        this.variable = piType.variable;
        this.type = piType.type.contextExpression(environment);
        this.body = piType.body.contextExpression(environment.appendType(piType.variable, piType.type));
    }

    public ContextPiType(Symbol variable, ContextExpression type, ContextExpression body) throws PouletException {
        super(new PiType(variable, type.expression, body.expression), type.environment);
        this.variable = variable;
        this.type = type;
        this.body = body;
    }

    public <T> T accept(ContextExpressionVisitor<T> visitor) throws PouletException {
        return visitor.visit(this);
    }
}
