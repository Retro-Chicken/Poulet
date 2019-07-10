package poulet.contextexpressions;

import poulet.ast.*;
import poulet.exceptions.PouletException;
import poulet.interpreter.Evaluator;
import poulet.typing.Checker;
import poulet.typing.Environment;
import poulet.util.ContextExpressionVisitor;
import poulet.util.PiTypeDecomposition;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ContextMatch extends ContextExpression {
    public final ContextExpression expression;
    public final Symbol expressionSymbol;
    public final List<Symbol> argumentSymbols;
    public final ContextExpression type;
    public final List<Clause> clauses;

    public ContextMatch(ContextExpression expression, Symbol expressionSymbol, List<Symbol> argumentSymbols, ContextExpression type, List<Clause> clauses) throws PouletException {
        super(new Match(expression.expression, expressionSymbol, argumentSymbols, type.expression,
                clauses.stream().map(x -> new Match.Clause(x.constructorSymbol, x.argumentSymbols, x.expression.expression)).collect(Collectors.toList())), expression.environment);
        this.expression = expression;
        this.expressionSymbol = expressionSymbol;
        this.argumentSymbols = argumentSymbols;
        this.type = type;
        this.clauses = clauses;
    }

    public ContextMatch(Match match, Environment environment) throws PouletException {
        super(match, environment);
        Expression expressionType = Checker.deduceType(match.expression, environment);
        if(!(expressionType instanceof InductiveType)) {
            System.out.println("\n\n" + match.toString() + "\n\n" + match.expression.toString() + "\n\n" + Evaluator.reduce(match.expression, environment) + "\n\n" + environment.toString() + "\n\n" + Checker.deduceType(Evaluator.reduce(match.expression, environment), environment));
            throw new PouletException("Matching on non-inductive type of class " + expressionType.getClass().getSimpleName());
        }

        // Add basic stuff
        this.expressionSymbol = match.expressionSymbol;
        this.argumentSymbols = match.argumentSymbols;
        this.expression = match.expression.contextExpression(environment);

        // Checking for errors
        InductiveType inductiveType = (InductiveType) expressionType;
        TypeDeclaration typeDeclaration = environment.lookUpTypeDeclaration(inductiveType.type);
        if (typeDeclaration == null)
            throw new PouletException("type declaration " + inductiveType.type + " not found");
        PiTypeDecomposition piTypeDecomposition = new PiTypeDecomposition(typeDeclaration.type);
        if (match.argumentSymbols.size() != piTypeDecomposition.argumentTypes.size())
            throw new PouletException("wrong number of arguments");

        // Get proper environment for type
        Environment newEnvironment = environment.appendScope(match.expressionSymbol, match.expression);
        for (int i = 0; i < piTypeDecomposition.argumentTypes.size(); i++) {
            Symbol symbol = match.argumentSymbols.get(i);
            Expression argument = piTypeDecomposition.argumentTypes.get(i);
            newEnvironment = newEnvironment.appendType(symbol, argument);
            // TODO: need to add to scope too?
        }
        this.type = match.type.contextExpression(newEnvironment);

        // Get proper environments for each clause
        List<Clause> clauses = new ArrayList<>();
        for (Constructor constructor : typeDeclaration.constructors) {
            Match.Clause matchingClause = null;
            newEnvironment = environment;

            for (Match.Clause clause : match.clauses) {
                if (clause.constructorSymbol.equals(constructor.name)) {
                    matchingClause = clause;
                    break;
                }
            }

            if (matchingClause == null)
                throw new PouletException("no matching clause for constructor " + constructor.name);

            for (int i = 0; i < typeDeclaration.parameters.size(); i++) {
                newEnvironment = newEnvironment.appendScope(
                        typeDeclaration.parameters.get(i).symbol,
                        inductiveType.parameters.get(i)
                );
            }

            PiTypeDecomposition constructorPiTypeDecomposition = new PiTypeDecomposition(constructor.definition);

            if (matchingClause.argumentSymbols.size() != constructorPiTypeDecomposition.argumentTypes.size())
                throw new PouletException("wrong number of arguments");

            for (int i = 0; i < constructorPiTypeDecomposition.argumentTypes.size(); i++) {
                Symbol symbol = matchingClause.argumentSymbols.get(i);
                Expression argumentType = constructorPiTypeDecomposition.argumentTypes.get(i);
                newEnvironment = newEnvironment.appendType(symbol, argumentType);
            }

            List<Expression> arguments = new ArrayList<>();

            for (Symbol symbol : matchingClause.argumentSymbols) {
                // TODO: is uniqueness working here?
                arguments.add(new Variable(symbol));
            }

            Expression newExpression = new ConstructorCall(
                    inductiveType,
                    constructor.name,
                    arguments
            );
            newEnvironment = newEnvironment.appendScope(match.expressionSymbol, newExpression);
            clauses.add(new Clause(matchingClause, newEnvironment));
        }
        this.clauses = clauses;
    }

    public static class Clause {
        public final Symbol constructorSymbol;
        public final List<Symbol> argumentSymbols;
        public final ContextExpression expression;

        public Clause(Match.Clause clause, Environment environment) throws PouletException {
            this.constructorSymbol = clause.constructorSymbol;
            this.argumentSymbols = clause.argumentSymbols;
            this.expression = clause.expression.contextExpression(environment);
        }

        public Clause(Symbol constructorSymbol, List<Symbol> argumentSymbols, ContextExpression expression) {
            this.constructorSymbol = constructorSymbol;
            this.argumentSymbols = argumentSymbols;
            this.expression = expression;
        }
    }

    public Clause getClause(Symbol constructor) throws PouletException {
        for (Clause clause : clauses) {
            if (clause.constructorSymbol.equals(constructor)) {
                return clause;
            }
        }

        throw new PouletException("no clause for " + constructor + " in " + this);
    }

    public <T> T accept(ContextExpressionVisitor<T> visitor) throws PouletException {
        return visitor.visit(this);
    }
}
