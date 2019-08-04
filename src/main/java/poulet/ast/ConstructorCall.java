package poulet.ast;

import static poulet.util.ListUtil.map;

import java.util.List;

public class ConstructorCall extends Expression {
    public Symbol inductiveType;
    public List<Expression> parameters;
    public Symbol constructor;
    public List<Expression> arguments;

    public ConstructorCall(Symbol inductiveType, List<Expression> parameters, Symbol constructor, List<Expression> arguments) {
        this.inductiveType = inductiveType;
        this.parameters = parameters;
        this.constructor = constructor;
        this.arguments = arguments;
    }

    @Override
    public poulet.kernel.ast.Expression compile() {
        return new poulet.kernel.ast.ConstructorCall(
                inductiveType.compile(),
                map(parameters, Expression::compile),
                constructor.compile(),
                map(arguments, Expression::compile)
        );
    }
}
