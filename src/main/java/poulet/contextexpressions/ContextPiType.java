package poulet.contextexpressions;

import poulet.ast.PiType;
import poulet.ast.Symbol;
import poulet.exceptions.PouletException;
import poulet.typing.Environment;

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
}
