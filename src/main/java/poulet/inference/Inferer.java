package poulet.inference;

import poulet.ast.Expression;
import poulet.ast.Symbol;
import poulet.contextexpressions.ContextAbstraction;
import poulet.contextexpressions.ContextApplication;
import poulet.contextexpressions.ContextExpression;
import poulet.contextexpressions.ContextVariable;
import poulet.exceptions.PouletException;
import poulet.interpreter.Evaluator;
import poulet.typing.Checker;
import poulet.util.ExpressionStepper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Inferer {
    public static ContextExpression inferApplication(ContextExpression expression) throws PouletException {
        if(!(expression instanceof ContextApplication))
            return expression;
        ContextApplication application = (ContextApplication) expression;
        ContextExpression current = application.function;
        ContextExpression argument = application.argument;
        ContextExpression argumentType = Checker.deduceType(argument);
        List<Symbol> targets = new ArrayList<>();
        while(true) {
            if(!(current instanceof ContextAbstraction)) {
                if(targets.isEmpty())
                    return expression;
                else
                    throw new PouletException("Argument type mismatch");
            }
            ContextAbstraction function = (ContextAbstraction) current;
            try {
                infer(function.type, argumentType.expression, targets);

                Map<Symbol, Expression> inferences = infer(Evaluator.reduce(function.type), Evaluator.reduce(argumentType).expression,
                        targets, new HashMap<>());
                ContextExpression result = function.body;
                for(Symbol symbol : inferences.keySet())
                    result = result.appendScope(symbol, inferences.get(symbol));

                return result.appendScope(function.symbol, argument.expression);
            } catch (PouletException exception) {
                if(function.inferable == true)
                    targets.add(function.symbol);
                else
                    throw new PouletException("Argument type mismatch");
            } finally {
                current = function.body;
            }
        }
    }

    public static ContextExpression infer(ContextExpression base, Expression pattern, List<Symbol> targets) throws PouletException {
        Map<Symbol, Expression> inferences = infer(Evaluator.reduce(base), Evaluator.reduce(pattern, base.environment),
                targets, new HashMap<>());
        ContextExpression result = base;
        for(Symbol symbol : inferences.keySet())
            result = result.appendScope(symbol, inferences.get(symbol));
        if(!Evaluator.convertible(result.expression, pattern, result.environment))
            throw new PouletException("Inferer failed to produce convertible expressions");
        return result;
    }

    private static Map<Symbol, Expression> infer(ContextExpression base, Expression pattern,
                                                List<Symbol> targets, Map<Symbol, Expression> current) throws PouletException {
        Map<Symbol, Expression> result = new HashMap<>(current);
        if(base instanceof ContextVariable) {
            Symbol symbol = ((ContextVariable) base).symbol;
            if(targets.contains(symbol)) {
                if(!result.containsKey(symbol)) {
                    result.put(symbol, pattern);
                    return result;
                } else {
                    if(Evaluator.convertible(result.get(symbol), pattern, base.environment))
                        return result;
                    else
                        throw new PouletException("Inference Error: Conflicting symbol inferences");
                }
            }
        }
        if(base.expression.getClass() != pattern.getClass())
            throw new PouletException("Inference Error: Base expression does not match pattern: " + base.expression + ", " + pattern);

        List<ContextExpression> steps = ExpressionStepper.steps(base);
        List<Expression> patternSteps = ExpressionStepper.steps(pattern);
        if(steps.size() != patternSteps.size())
            throw new PouletException("Inference Error: Base expression does not match pattern");
        for(int i = 0; i < steps.size(); i++)
            result.putAll(infer(steps.get(i), patternSteps.get(i), targets, result));

        return result;
    }
}
