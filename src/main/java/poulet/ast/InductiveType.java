package poulet.ast;

import java.util.List;
import static poulet.util.ListUtil.map;

public class InductiveType extends Expression {
    public Symbol inductiveType;
    public List<Expression> parameters;
    public List<Expression> arguments;

    public InductiveType(Symbol inductiveType, List<Expression> parameters, List<Expression> arguments) {
        this.inductiveType = inductiveType;
        this.parameters = parameters;
        this.arguments = arguments;
    }

    @Override
    public poulet.kernel.ast.Expression compile() {
        return new poulet.kernel.ast.InductiveType(
                inductiveType.compile(),
                map(parameters, Expression::compile),
                map(arguments, Expression::compile)
        );
    }
}
