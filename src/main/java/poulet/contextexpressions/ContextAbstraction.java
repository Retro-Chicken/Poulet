package poulet.contextexpressions;

import poulet.ast.Abstraction;
import poulet.ast.Symbol;
import poulet.typing.Environment;

public class ContextAbstraction extends ContextExpression {
    public final Symbol symbol;
    public final ContextExpression type;
    public final ContextExpression body;

    public ContextAbstraction(Abstraction abstraction, Environment environment) {
        super(abstraction, environment);
        this.symbol = abstraction.symbol;
        this.type = abstraction.type.contextExpression(environment);
        this.body = abstraction.body.contextExpression(environment.appendType(abstraction.symbol, abstraction.type));
    }
}
