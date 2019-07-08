package poulet.contextexpressions;

import poulet.ast.Prop;
import poulet.typing.Environment;

public class ContextProp extends ContextExpression {
    public ContextProp(Prop prop, Environment environment) {
        super(prop, environment);
    }
}
