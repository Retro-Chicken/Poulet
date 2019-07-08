package poulet.contextexpressions;

import poulet.ast.InductiveType;
import poulet.ast.Symbol;
import poulet.typing.Environment;

import java.util.List;
import java.util.stream.Collectors;

public class ContextInductiveType extends ContextExpression {
    public final Symbol type;
    private final boolean concrete;
    public final List<ContextExpression> parameters;
    public final List<ContextExpression> arguments;

    public ContextInductiveType(InductiveType inductiveType, Environment environment) {
        super(inductiveType, environment);
        this.type = inductiveType.type;
        this.concrete = inductiveType.isConcrete();
        this.parameters = inductiveType.parameters.stream().map(x -> x.contextExpression(environment)).collect(Collectors.toList());
        this.arguments = inductiveType.arguments.stream().map(x -> x.contextExpression(environment)).collect(Collectors.toList());
    }

    public boolean isConcrete() {
        return concrete;
    }
}
