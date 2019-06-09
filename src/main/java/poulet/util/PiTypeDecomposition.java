package poulet.util;

import poulet.ast.Expression;
import poulet.ast.PiType;
import poulet.ast.Symbol;

import java.util.ArrayList;
import java.util.List;

public class PiTypeDecomposition {
    public List<Symbol> arguments = new ArrayList<>();
    public List<Expression> argumentTypes = new ArrayList<>();
    public Expression bodyType = null;

    public PiTypeDecomposition(Expression expression) {
        decompose(expression);
    }

    private void decompose(Expression expression) {
        if (expression instanceof PiType) {
            PiType piType = (PiType) expression;
            arguments.add(piType.variable);
            argumentTypes.add(piType.type);
            decompose(piType.body);
        } else {
            bodyType = expression;
        }
    }
}
