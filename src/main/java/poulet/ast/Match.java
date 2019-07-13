package poulet.ast;

import poulet.typing.Checker;
import poulet.typing.Environment;
import poulet.util.PiTypeDecomposition;
import poulet.util.StringUtil;
import poulet.exceptions.PouletException;
import poulet.util.ExpressionVisitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Match extends Expression {
    public final Expression expression;
    public final Symbol expressionSymbol;
    public final List<Symbol> argumentSymbols;
    public final Expression type;
    public final List<Clause> clauses;

    public Match(Expression expression, Symbol expressionSymbol, List<Symbol> argumentSymbols, Expression type, List<Clause> clauses, Environment environment) throws PouletException {
        super(environment);
        // Add basic stuff
        this.expressionSymbol = expressionSymbol;
        this.argumentSymbols = argumentSymbols;
        this.expression = expression.context(environment);

        if(environment == null) {
            this.type = type.context(null);
            this.clauses = clauses.stream().map(clause -> new Clause(clause.constructorSymbol, clause.argumentSymbols, clause.expression.context(null))).collect(Collectors.toList());
            return;
        }

        Expression expressionType = Checker.deduceType(expression, environment);
        if(!(expressionType instanceof InductiveType))
            throw new PouletException("Matching on non-inductive type of class " + expressionType.getClass().getSimpleName());

        // Checking for errors
        InductiveType inductiveType = (InductiveType) expressionType;
        TypeDeclaration typeDeclaration = environment.lookUpTypeDeclaration(inductiveType.type);
        if (typeDeclaration == null)
            throw new PouletException("type declaration " + inductiveType.type + " not found");
        PiTypeDecomposition piTypeDecomposition = new PiTypeDecomposition(typeDeclaration.type);
        if (argumentSymbols.size() != piTypeDecomposition.argumentTypes.size())
            throw new PouletException("wrong number of arguments");

        // Get proper environment for type
        Environment newEnvironment = environment.appendScope(expressionSymbol, expression);
        for (int i = 0; i < piTypeDecomposition.argumentTypes.size(); i++) {
            Symbol symbol = argumentSymbols.get(i);
            Expression argument = piTypeDecomposition.argumentTypes.get(i);
            newEnvironment = newEnvironment.appendType(symbol, argument);
            // TODO: need to add to scope too?
        }
        this.type = type.context(newEnvironment);

        // Get proper environments for each clause
        List<Clause> newClauses = new ArrayList<>();
        for (Constructor constructor : typeDeclaration.constructors) {
            Clause matchingClause = null;
            newEnvironment = environment;

            for (Clause clause : clauses) {
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
                arguments.add(new Variable(symbol, environment));
            }

            Expression newExpression = new ConstructorCall(
                    inductiveType,
                    constructor.name,
                    arguments
            );
            newEnvironment = newEnvironment.appendScope(expressionSymbol, newExpression);
            newClauses.add(new Clause(matchingClause.constructorSymbol, matchingClause.argumentSymbols, matchingClause.expression.context(newEnvironment)));
        }
        this.clauses = newClauses;
    }

    public Match(Expression expression, Symbol expressionSymbol, List<Symbol> argumentSymbols, Expression type, List<Clause> clauses) throws PouletException {
        this(expression, expressionSymbol, argumentSymbols, type, clauses, null);
    }

    public Clause getClause(Symbol constructor) throws PouletException {
        for (Clause clause : clauses) {
            if (clause.constructorSymbol.equals(constructor)) {
                return clause;
            }
        }

        throw new PouletException("no clause for " + constructor + " in " + this);
    }

    @Override
    public String toString() {
        String items = clauses.stream().map(Clause::toString).collect(Collectors.joining(",\n"));

        String s = "match " + expression + " as " + expressionSymbol + "(";
        s += argumentSymbols.stream().map(Symbol::toString).collect(Collectors.joining(", "));
        s += ") in " + type + " {\n";
        s += StringUtil.indent(items, 2);
        s += "\n}";
        return s;
    }

    public static class Clause extends Node {
        public final Symbol constructorSymbol;
        public final List<Symbol> argumentSymbols;
        public final Expression expression;

        public Clause(Symbol constructorSymbol, List<Symbol> argumentSymbols, Expression expression) {
            this.constructorSymbol = constructorSymbol;
            this.argumentSymbols = argumentSymbols;
            this.expression = expression;
        }

        @Override
        public String toString() {
            String s = "" + constructorSymbol + "(";
            s += argumentSymbols.stream().map(Symbol::toString).collect(Collectors.joining(", "));
            s += ") => " + expression;
            return s;
        }
    }

    @Override
    Match transformSymbols(Function<Symbol, Symbol> transformer, Map<Symbol, Symbol> map) throws PouletException {
        Map<Symbol, Symbol> newMap = new HashMap<>(map);
        Symbol newExpressionSymbol = transformer.apply(expressionSymbol);
        newMap.put(expressionSymbol, newExpressionSymbol);

        List<Symbol> newArgumentSymbols = new ArrayList<>();
        for (Symbol argumentSymbol : argumentSymbols) {
            Symbol newArgumentSymbol = transformer.apply(argumentSymbol);
            newArgumentSymbols.add(newArgumentSymbol);
            newMap.put(argumentSymbol, newArgumentSymbol);
        }

        Expression newType = type.transformSymbols(transformer, newMap);

        List<Match.Clause> newClauses = new ArrayList<>();

        for (Match.Clause clause : clauses) {
            Map<Symbol, Symbol> clauseNewMap = new HashMap<>(map);

            List<Symbol> clauseArgumentSymbols = new ArrayList<>();
            for (Symbol clauseArgumentSymbol : clause.argumentSymbols) {
                Symbol newClauseArgumentSymbol = transformer.apply(clauseArgumentSymbol);
                clauseArgumentSymbols.add(newClauseArgumentSymbol);
                clauseNewMap.put(clauseArgumentSymbol, newClauseArgumentSymbol);
            }

            Match.Clause newClause = new Match.Clause(
                    clause.constructorSymbol,
                    clauseArgumentSymbols,
                    clause.expression.transformSymbols(transformer, clauseNewMap)
            );
            newClauses.add(newClause);
        }

        return new Match(
                expression.transformSymbols(transformer, map),
                newExpressionSymbol,
                newArgumentSymbols,
                newType,
                newClauses,
                environment
        );
    }

    public <T> T accept(ExpressionVisitor<T> visitor) throws PouletException {
        return visitor.visit(this);
    }

    public Match context(Environment environment) throws PouletException {
        return new Match(expression, expressionSymbol, argumentSymbols, type, clauses, environment);
    }
}
