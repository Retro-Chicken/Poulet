package poulet.contextexpressions;

import poulet.ast.Expression;
import poulet.ast.InductiveType;
import poulet.ast.Symbol;
import poulet.exceptions.PouletException;
import poulet.typing.Environment;
import poulet.util.ContextExpressionVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        List<ContextExpression> arguments = null;
        if(inductiveType.arguments != null) {
            arguments = new ArrayList<>();
            for (Expression argument : inductiveType.arguments)
                arguments.add(argument.contextExpression(environment));
        }
        this.arguments = arguments;
    }

    public ContextInductiveType(Symbol type, boolean concrete, List<ContextExpression> parameters) throws PouletException {
        super(new InductiveType(type, concrete, parameters.stream().map(x -> x.expression).collect(Collectors.toList())), parameters.size() > 0 ? parameters.get(0).environment : null);
        this.type = type;
        this.concrete = concrete;
        this.parameters = parameters;
        this.arguments = concrete ? new ArrayList<>() : null;
    }

    public ContextInductiveType(Symbol type, boolean concrete, List<ContextExpression> parameters, List<ContextExpression> arguments, Environment environment) throws PouletException {
        super(new InductiveType(type, concrete, parameters.stream().map(x -> x.expression).collect(Collectors.toList()),
                arguments.stream().map(x -> x.expression).collect(Collectors.toList())), environment);
        if (concrete && arguments == null) {
            System.out.println(type);
            System.out.println(parameters);
            System.out.println(">>>the fuck");
        }

        this.type = type;
        this.concrete = concrete;
        this.parameters = parameters;
        this.arguments = arguments;
    }

    public boolean isConcrete() {
        return concrete;
    }

    public <T> T accept(ContextExpressionVisitor<T> visitor) throws PouletException {
        return visitor.visit(this);
    }
}
