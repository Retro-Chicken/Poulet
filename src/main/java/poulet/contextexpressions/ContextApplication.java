package poulet.contextexpressions;

import poulet.ast.Application;
import poulet.typing.Environment;

public class ContextApplication extends ContextExpression {
    public final ContextExpression function;
    public final ContextExpression argument;

    public ContextApplication(Application application, Environment environment) {
        super(application, environment);
        this.function = application.function.contextExpression(environment);
        this.argument = application.argument.contextExpression(environment);
    }
}
