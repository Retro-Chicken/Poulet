package poulet.inference;

import poulet.ast.Abstraction;
import poulet.ast.Expression;
import poulet.ast.Symbol;
import poulet.contextexpressions.*;
import poulet.exceptions.PouletException;
import poulet.interpreter.Evaluator;
import poulet.typing.Checker;
import poulet.typing.Environment;
import poulet.util.ExpressionStepper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Inferer {
    public static ContextApplication fillImplicitArguments(ContextApplication expression) throws PouletException {
        List<Expression> implicitArguments = getImplicitArguments(expression);
        if(implicitArguments.isEmpty())
            return expression;
        ContextApplication result = new ContextApplication(expression.function, implicitArguments.get(0).contextExpression(expression.environment));
        for(int i = 1; i < implicitArguments.size(); i++)
            result = new ContextApplication(result, implicitArguments.get(i).contextExpression(expression.environment));
        result = new ContextApplication(result, expression.argument);
        return result;
    }

    private static List<Expression> getImplicitArguments(ContextApplication application) throws PouletException {
        ContextExpression signature = Checker.deduceType(application.function);
        ContextExpression argumentType = Checker.deduceType(application.argument);
        List<Symbol> targets = new ArrayList<>();
        while(true) {
            if(!(signature instanceof ContextPiType)) {
                if(targets.isEmpty())
                    return new ArrayList<>();
                else
                    throw new PouletException("Argument type mismatch");
            }
            ContextPiType piType = (ContextPiType) signature;
            try {
                Map<Symbol, Expression> inferences = safeInfer(piType.type, argumentType.expression,
                        targets, new HashMap<>());
                List<Expression> result = new ArrayList<>();
                for(Symbol symbol : targets)
                    result.add(inferences.get(symbol));
                return result;
            } catch (PouletException exception) {
                if(piType.inferable)
                    targets.add(piType.variable);
                else
                    throw new PouletException("Argument type mismatch");
            } finally {
                signature = piType.body;
            }
        }
    }

    private static Map<Symbol, Expression> safeInfer(ContextExpression base, Expression pattern,
                                                     List<Symbol> targets, Map<Symbol, Expression> current) throws PouletException {
        Map<Symbol, Expression> result = infer(base, pattern, targets, current);

        Environment newEnvironment = base.environment;
        for(Symbol symbol : result.keySet())
            newEnvironment = newEnvironment.appendScope(symbol, result.get(symbol));
        if(!Evaluator.convertible(base.expression, pattern, newEnvironment))
            throw new PouletException("Inferer failed to produce convertible expressions");

        return result;
    }

    private static Map<Symbol, Expression> infer(ContextExpression base, Expression pattern,
                                                List<Symbol> targets, Map<Symbol, Expression> current) throws PouletException {
        if(targets.isEmpty())
            return new HashMap<>();
        Map<Symbol, Expression> result = new HashMap<>(current);
        if (base instanceof ContextVariable) {
            Symbol symbol = ((ContextVariable) base).symbol;
            if (targets.contains(symbol)) {
                if (!result.containsKey(symbol)) {
                    result.put(symbol, pattern);
                    return result;
                } else {
                    if (Evaluator.convertible(result.get(symbol), pattern, base.environment))
                        return result;
                    else
                        throw new PouletException("Inference Error: Conflicting symbol inferences");
                }
            }
        }
        if (base.expression.getClass() != pattern.getClass())
            throw new PouletException("Inference Error: Base expression does not match pattern: " + base.expression + ", " + pattern);

        List<ContextExpression> steps = ExpressionStepper.steps(base);
        List<Expression> patternSteps = ExpressionStepper.steps(pattern);
        if (steps.size() != patternSteps.size())
            throw new PouletException("Inference Error: Base expression does not match pattern");
        for (int i = 0; i < steps.size(); i++)
            result.putAll(infer(steps.get(i), patternSteps.get(i), targets, result));

        return result;
    }
}
