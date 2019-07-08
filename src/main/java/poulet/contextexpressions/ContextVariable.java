package poulet.contextexpressions;

import poulet.ast.Variable;
import poulet.typing.Environment;

public class ContextVariable extends ContextExpression {
    public ContextVariable(Variable variable, Environment environment) {
        super(variable, environment);
    }
}
