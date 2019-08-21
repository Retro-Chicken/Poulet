package poulet.superficial.decomposition;

import poulet.superficial.ast.expressions.Application;
import poulet.superficial.ast.expressions.Expression;

import java.util.ArrayList;
import java.util.List;

public class ApplicationDecomposition {
    public Expression function = null;
    public List<Expression> arguments = new ArrayList<>();

    // decomposes into M = f(a_1, ..., a_n) with f not being an application
    public ApplicationDecomposition(Expression expression) {
        decompose(expression);
    }

    public ApplicationDecomposition(Expression function, List<Expression> arguments) {
        this.function = function;
        this.arguments = arguments;
    }

    private void decompose(Expression expression) {
        if (expression instanceof Application) {
            Application application = (Application) expression;
            arguments.add(0, application.argument);
            decompose(application.function);
        } else {
            function = expression;
        }
    }

    public Expression expression() {
        Expression expression = function;

        for (Expression argument : arguments) {
            expression = new Application(
                    expression,
                    argument
            );
        }

        return expression;
    }
}
