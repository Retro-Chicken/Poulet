package poulet.kernel.decomposition;

import poulet.kernel.ast.Abstraction;
import poulet.kernel.ast.Expression;
import poulet.kernel.ast.Symbol;

import java.util.ArrayList;
import java.util.List;

public class ArgumentDecomposition {
    public List<Symbol> arguments = new ArrayList<>();
    public List<Expression> argumentTypes = new ArrayList<>();
    public Expression body;

    public ArgumentDecomposition(Expression expression) {
        decompose(expression);
    }

    private void decompose(Expression expression) {
        if (expression instanceof Abstraction) {
            Abstraction abstraction = (Abstraction) expression;
            arguments.add(abstraction.argumentSymbol);
            argumentTypes.add(abstraction.argumentType);
            Expression body = abstraction.body;
            decompose(body);
        } else {
            body = expression;
        }
    }
}
