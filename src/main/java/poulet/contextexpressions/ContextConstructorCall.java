package poulet.contextexpressions;

import poulet.ast.ConstructorCall;
import poulet.ast.Expression;
import poulet.ast.Symbol;
import poulet.exceptions.PouletException;
import poulet.typing.Environment;
import poulet.util.ContextExpressionVisitor;

import java.util.ArrayList;
import java.util.List;

public class ContextConstructorCall extends ContextExpression {
    public final ContextInductiveType inductiveType;
    public final Symbol constructor;
    public final List<ContextExpression> arguments;

    public ContextConstructorCall(ConstructorCall constructorCall, Environment environment) throws PouletException {
        super(constructorCall, environment);
        this.inductiveType = constructorCall.inductiveType.contextExpression(environment);
        this.constructor = constructorCall.constructor;
        List<ContextExpression> arguments = null;
        if(constructorCall.arguments != null) {
            arguments = new ArrayList<>();
            for (Expression argument : constructorCall.arguments)
                arguments.add(argument.contextExpression(environment));
        }
        this.arguments = arguments;
    }

    public boolean isConcrete() {
        return arguments != null;
    }

    public <T> T accept(ContextExpressionVisitor<T> visitor) throws PouletException {
        return visitor.visit(this);
    }
}
