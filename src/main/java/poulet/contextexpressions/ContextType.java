package poulet.contextexpressions;

import poulet.ast.Type;
import poulet.typing.Environment;

public class ContextType extends ContextExpression {
    public ContextType(Type type, Environment environment) {
        super(type, environment);
    }
}
