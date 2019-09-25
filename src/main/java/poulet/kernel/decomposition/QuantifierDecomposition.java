package poulet.kernel.decomposition;

import poulet.kernel.ast.Abstraction;
import poulet.kernel.ast.Expression;
import poulet.kernel.ast.Prod;
import poulet.kernel.ast.Symbol;

import java.util.ArrayList;
import java.util.List;

public class QuantifierDecomposition {
    public List<Symbol> arguments = new ArrayList<>();
    public List<Expression> argumentTypes = new ArrayList<>();
    public Expression body;

    private List<QuantifierTypes> quantifierTypes = new ArrayList<>();

    enum QuantifierTypes {
        ABSTRACTION,
        PRODUCT
    }

    public QuantifierDecomposition(Expression expression) {
        decompose(expression);
    }

    public QuantifierDecomposition(List<Symbol> arguments, List<Expression> argumentTypes, Expression body) {
        this.arguments = arguments;
        this.argumentTypes = argumentTypes;
        this.body = body;
    }

    private void decompose(Expression expression) {
        if (expression instanceof Abstraction) {
            quantifierTypes.add(QuantifierTypes.ABSTRACTION);
            Abstraction abstraction = (Abstraction) expression;
            arguments.add(abstraction.argumentSymbol);
            argumentTypes.add(abstraction.argumentType);
            Expression body = abstraction.body;
            decompose(body);
        } else if (expression instanceof Prod) {
            quantifierTypes.add(QuantifierTypes.PRODUCT);
            Prod product = (Prod) expression;
            arguments.add(product.argumentSymbol);
            argumentTypes.add(product.argumentType);
            Expression body = product.bodyType;
            decompose(body);
        } else {
            body = expression;
        }
    }

    public Expression expression() {
        Expression expression = body;

        for (int i = arguments.size() - 1; i >= 0; i--) {
            switch (quantifierTypes.get(i)) {
                case ABSTRACTION:
                    expression = new Abstraction(
                            arguments.get(i),
                            argumentTypes.get(i),
                            expression
                    );
                    break;
                case PRODUCT:
                    expression = new Prod(
                            arguments.get(i),
                            argumentTypes.get(i),
                            expression
                    );
                    break;
            }
        }

        return expression;
    }
}
