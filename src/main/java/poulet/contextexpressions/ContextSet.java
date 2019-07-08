package poulet.contextexpressions;

import poulet.ast.Set;
import poulet.typing.Environment;

public class ContextSet extends ContextExpression {
    public ContextSet(Set set, Environment environment) {
        super(set, environment);
    }
}
