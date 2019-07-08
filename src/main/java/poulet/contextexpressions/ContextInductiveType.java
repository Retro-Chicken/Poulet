package poulet.contextexpressions;

import poulet.ast.Expression;
import poulet.ast.InductiveType;
import poulet.ast.Symbol;
import poulet.exceptions.PouletException;
import poulet.typing.Environment;
import poulet.util.ContextExpressionVisitor;

import java.util.ArrayList;
import java.util.List;

public class ContextInductiveType extends ContextExpression {
    public final Symbol type;
    private final boolean concrete;
    public final List<ContextExpression> parameters;
    public final List<ContextExpression> arguments;

    public ContextInductiveType(InductiveType inductiveType, Environment environment) throws PouletException {
        super(inductiveType, environment);
        this.type = inductiveType.type;
        this.concrete = inductiveType.isConcrete();
        List<ContextExpression> parameters = new ArrayList<>();
        for(Expression parameter : inductiveType.parameters)
            parameters.add(parameter.contextExpression(environment));
        this.parameters = parameters;
        List<ContextExpression> arguments = new ArrayList<>();
        for(Expression argument : inductiveType.arguments)
            arguments.add(argument.contextExpression(environment));
        this.arguments = arguments;
    }

    public boolean isConcrete() {
        return concrete;
    }

    public <T> T accept(ContextExpressionVisitor<T> visitor) throws PouletException {
        return visitor.visit(this);
    }
}
