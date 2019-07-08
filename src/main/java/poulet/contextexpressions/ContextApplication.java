package poulet.contextexpressions;

import poulet.ast.Application;
import poulet.exceptions.PouletException;
import poulet.typing.Environment;
import poulet.util.ContextExpressionVisitor;

public class ContextApplication extends ContextExpression {
    public final ContextExpression function;
    public final ContextExpression argument;

    public ContextApplication(Application application, Environment environment) throws PouletException {
        super(application, environment);
        this.function = application.function.contextExpression(environment);
        this.argument = application.argument.contextExpression(environment);
    }

    public <T> T accept(ContextExpressionVisitor<T> visitor) throws PouletException {
        return visitor.visit(this);
    }
}
