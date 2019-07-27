package poulet.kernel.decomposition;

import poulet.kernel.ast.Abstraction;
import poulet.kernel.ast.Expression;
import poulet.kernel.ast.Symbol;

import java.util.ArrayList;
import java.util.List;

public class AbstractionDecomposition {
    public List<Symbol> arguments = new ArrayList<>();
    public List<Expression> argumentTypes = new ArrayList<>();
    public Expression body;

    public AbstractionDecomposition(Expression expression) {
        decompose(expression);
    }

    public AbstractionDecomposition(List<Symbol> arguments, List<Expression> argumentTypes, Expression body) {
        this.arguments = arguments;
        this.argumentTypes = argumentTypes;
        this.body = body;
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

    public Expression expression() {
        Expression expression = body;

        for (int i = arguments.size() - 1; i >= 0; i--) {
            expression = new Abstraction(
                    arguments.get(i),
                    argumentTypes.get(i),
                    expression
            );
        }

        return expression;
    }
}
