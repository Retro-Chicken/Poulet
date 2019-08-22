package poulet.superficial;

import poulet.superficial.ast.expressions.*;
import poulet.superficial.ast.inlines.LetIn;
import poulet.superficial.ast.inlines.Where;

import java.util.stream.Collectors;

public class Substituter {
    public static Expression substitute(Expression base, Symbol symbol, Expression substitute) {
        return base.accept(new ExpressionVisitor<>() {
            @Override
            public Abstraction visit(Abstraction abstraction) {
                return new Abstraction(
                        abstraction.argumentSymbol,
                        substitute(abstraction.argumentType, symbol, substitute),
                        substitute(abstraction.body, symbol, substitute)
                );
            }

            @Override
            public Application visit(Application application) {
                return new Application(
                        substitute(application.function, symbol, substitute),
                        substitute(application.argument, symbol, substitute)
                );
            }

            @Override
            public ConstructorCall visit(ConstructorCall constructorCall) {
                return new ConstructorCall(
                        constructorCall.inductiveType,
                        constructorCall.parameters.stream().map(x -> substitute(x, symbol, substitute)).collect(Collectors.toList()),
                        constructorCall.constructor,
                        constructorCall.arguments.stream().map(x -> substitute(x, symbol, substitute)).collect(Collectors.toList())
                );
            }

            @Override
            public Fix visit(Fix fix) {
                return new Fix(
                        fix.clauses.stream().map(x -> new Fix.Clause(
                                x.symbol,
                                substitute(x.type, symbol, substitute),
                                substitute(x.definition, symbol, substitute)
                        )).collect(Collectors.toList()),
                        fix.mainSymbol
                );
            }

            @Override
            public InductiveType visit(InductiveType inductiveType) {
                return new InductiveType(
                        inductiveType.inductiveType,
                        inductiveType.parameters.stream().map(x -> substitute(x, symbol, substitute)).collect(Collectors.toList()),
                        inductiveType.arguments.stream().map(x -> substitute(x, symbol, substitute)).collect(Collectors.toList())
                );
            }

            @Override
            public Match visit(Match match) {
                return new Match(
                        substitute(match.expression, symbol, substitute),
                        match.expressionSymbol,
                        match.argumentSymbols,
                        substitute(match.type, symbol, substitute),
                        match.clauses.stream().map(x -> new Match.Clause(
                                x.constructor,
                                x.argumentSymbols,
                                substitute(x.expression, symbol, substitute)
                        )).collect(Collectors.toList())
                );
            }

            @Override
            public Prod visit(Prod prod) {
                return new Prod(
                        prod.argumentSymbol,
                        substitute(prod.argumentType, symbol, substitute),
                        substitute(prod.bodyType, symbol, substitute)
                );
            }

            @Override
            public Expression visit(Var var) {
                if(var.symbol.equals(symbol))
                    return substitute;
                else
                    return var;
            }

            @Override
            public LetIn visit(LetIn letIn) {
                return new LetIn(
                        letIn.symbol,
                        substitute(letIn.value, symbol, substitute),
                        substitute(letIn.body, symbol, substitute)
                );
            }

            @Override
            public Where visit(Where where) {
                return new Where(
                        where.symbol,
                        substitute(where.value, symbol, substitute),
                        substitute(where.body, symbol, substitute)
                );
            }

            @Override
            public Expression other(Expression expression) {
                return expression;
            }
        });
    }
}
