package poulet.kernel.decomposition;

import poulet.kernel.ast.Expression;
import poulet.kernel.ast.Prod;
import poulet.kernel.ast.Symbol;

import java.util.ArrayList;
import java.util.List;

public class ProdDecomposition {
    public List<Symbol> arguments = new ArrayList<>();
    public List<Expression> argumentTypes = new ArrayList<>();
    public Expression bodyType = null;

    public ProdDecomposition(Expression expression) {
        decompose(expression);
    }

    private void decompose(Expression expression) {
        if (expression instanceof Prod) {
            Prod prod = (Prod) expression;
            arguments.add(prod.argumentSymbol);
            argumentTypes.add(prod.argumentType);
            decompose(prod.bodyType);
        } else {
            bodyType = expression;
        }
    }

    public Expression expression() {
        Expression expression = bodyType;

        for (int i = arguments.size() - 1; i >= 0; i--) {
            expression = new Prod(
                    arguments.get(i),
                    argumentTypes.get(i),
                    expression
            );
        }

        return expression;
    }
}
