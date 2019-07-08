package poulet.contextexpressions;

import poulet.ast.ConstructorCall;
import poulet.ast.Symbol;
import poulet.typing.Environment;

import java.util.List;
import java.util.stream.Collectors;

public class ContextConstructorCall extends ContextExpression {
    public final ContextInductiveType inductiveType;
    public final Symbol constructor;
    public final List<ContextExpression> arguments;

    public ContextConstructorCall(ConstructorCall constructorCall, Environment environment) {
        super(constructorCall, environment);
        this.inductiveType = constructorCall.inductiveType.contextExpression(environment);
        this.constructor = constructorCall.constructor;
        this.arguments = constructorCall.arguments.stream().map(x -> x.contextExpression(environment)).collect(Collectors.toList());
    }
}
