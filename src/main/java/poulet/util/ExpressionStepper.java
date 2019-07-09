package poulet.util;

import poulet.ast.*;
import poulet.contextexpressions.*;
import poulet.exceptions.PouletException;

import java.util.ArrayList;
import java.util.List;

public class ExpressionStepper {
    public static List<ContextExpression> steps(ContextExpression expression) throws PouletException {
        return expression.accept(new ContextExpressionVisitor<>() {
            @Override
            public List<ContextExpression> visit(ContextAbstraction abstraction) {
                List<ContextExpression> steps = new ArrayList<>();
                steps.add(abstraction.type);
                steps.add(abstraction.body);
                return steps;
            }

            @Override
            public List<ContextExpression> visit(ContextApplication application) {
                List<ContextExpression> steps = new ArrayList<>();
                steps.add(application.function);
                steps.add(application.argument);
                return steps;
            }

            @Override
            public List<ContextExpression> visit(ContextConstructorCall constructorCall) {
                List<ContextExpression> steps = new ArrayList<>();
                steps.addAll(constructorCall.arguments);
                return steps;
            }

            @Override
            public List<ContextExpression> visit(ContextFix fix) {
                List<ContextExpression> steps = new ArrayList<>();
                for(ContextDefinition definition : fix.definitions) {
                    steps.add(definition.type);
                    steps.add(definition.definition);
                }
                return steps;
            }

            @Override
            public List<ContextExpression> visit(ContextInductiveType inductiveType) {
                List<ContextExpression> steps = new ArrayList<>();
                steps.addAll(inductiveType.parameters);
                steps.addAll(inductiveType.arguments);
                return steps;
            }

            @Override
            public List<ContextExpression> visit(ContextMatch match) {
                List<ContextExpression> steps = new ArrayList<>();
                steps.add(match.expression);
                steps.add(match.type);
                for(ContextMatch.Clause clause : match.clauses)
                    steps.add(clause.expression);
                return steps;
            }

            @Override
            public List<ContextExpression> visit(ContextPiType piType) {
                List<ContextExpression> steps = new ArrayList<>();
                steps.add(piType.type);
                steps.add(piType.body);
                return steps;
            }

            @Override
            public List<ContextExpression> other(ContextExpression expression) {
                return new ArrayList<>();
            }
        });
    }

    public static List<Expression> steps(Expression expression) throws PouletException {
        return expression.accept(new ExpressionVisitor<>() {
            @Override
            public List<Expression> visit(Abstraction abstraction) {
                List<Expression> steps = new ArrayList<>();
                steps.add(abstraction.type);
                steps.add(abstraction.body);
                return steps;
            }

            @Override
            public List<Expression> visit(Application application) {
                List<Expression> steps = new ArrayList<>();
                steps.add(application.function);
                steps.add(application.argument);
                return steps;
            }

            @Override
            public List<Expression> visit(ConstructorCall constructorCall) {
                List<Expression> steps = new ArrayList<>();
                steps.addAll(constructorCall.arguments);
                return steps;
            }

            @Override
            public List<Expression> visit(Fix fix) {
                List<Expression> steps = new ArrayList<>();
                for(Definition definition : fix.definitions) {
                    steps.add(definition.type);
                    steps.add(definition.definition);
                }
                return steps;
            }

            @Override
            public List<Expression> visit(InductiveType inductiveType) {
                List<Expression> steps = new ArrayList<>();
                steps.addAll(inductiveType.parameters);
                steps.addAll(inductiveType.arguments);
                return steps;
            }

            @Override
            public List<Expression> visit(Match match) {
                List<Expression> steps = new ArrayList<>();
                steps.add(match.expression);
                steps.add(match.type);
                for(Match.Clause clause : match.clauses)
                    steps.add(clause.expression);
                return steps;
            }

            @Override
            public List<Expression> visit(PiType piType) {
                List<Expression> steps = new ArrayList<>();
                steps.add(piType.type);
                steps.add(piType.body);
                return steps;
            }

            @Override
            public List<Expression> other(Expression expression) {
                return new ArrayList<>();
            }
        });
    }
}
