package poulet.ast;

import poulet.exceptions.PouletException;
import poulet.util.ExpressionVisitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class Expression extends Node {
    public Expression substitute(Symbol symbol, Expression substitution) throws PouletException {
        if (this instanceof Variable) {
            Variable variable = (Variable) this;
            if (variable.symbol.equals(symbol)) {
                return substitution;
            } else {
                return this;
            }
        } else if (this instanceof Application) {
            Application application = (Application) this;
            Expression function = application.function.substitute(symbol, substitution);
            Expression argument = application.argument.substitute(symbol, substitution);
            return new Application(function, argument);
        } else if (this instanceof Abstraction) {
            Abstraction abstraction = (Abstraction) this;
            if (abstraction.symbol != null && abstraction.symbol.equals(symbol)) {
                return this;
            } else {
                Expression type = abstraction.type.substitute(symbol, substitution);
                Expression body = abstraction.body.substitute(symbol, substitution);
                return new Abstraction(abstraction.symbol, type, body);
            }
        } else if (this instanceof PiType) {
            PiType piType = (PiType) this;
            Expression type = piType.type.substitute(symbol, substitution);
            Expression body = piType.body.substitute(symbol, substitution);
            return new PiType(piType.variable, type, body);
        } else if (this instanceof Match) {
            Match match = (Match) this;
            Expression matchExpression = match.expression.substitute(symbol, substitution);
            Expression type = match.type.substitute(symbol, substitution);
            List<Match.Clause> clauses = new ArrayList<>();

            for (Match.Clause clause : match.clauses) {
                Expression clauseExpression = clause.expression.substitute(symbol, substitution);
                Match.Clause newClause = new Match.Clause(
                        clause.constructorSymbol,
                        clause.argumentSymbols,
                        clauseExpression
                );
                clauses.add(newClause);
            }

            return new Match(
                    matchExpression,
                    match.expressionSymbol,
                    match.argumentSymbols,
                    type,
                    clauses
            );
        } else if (this instanceof InductiveType) {
            InductiveType inductiveType = (InductiveType) this;
            List<Expression> parameters = new ArrayList<>();

            for (Expression parameter : inductiveType.parameters) {
                parameters.add(parameter.substitute(symbol, substitution));
            }

            List<Expression> arguments = null;

            if (inductiveType.isConcrete()) {
                arguments = new ArrayList<>();

                for (Expression argument : inductiveType.arguments) {
                    arguments.add(argument.substitute(symbol, substitution));
                }
            }

            return new InductiveType(
                    inductiveType.type,
                    inductiveType.isConcrete(),
                    parameters,
                    arguments
            );
        } else if (this instanceof ConstructorCall) {
            ConstructorCall constructorCall = (ConstructorCall) this;

            List<Expression> arguments = null;

            if (constructorCall.isConcrete()) {
                arguments = new ArrayList<>();

                for (Expression argument : constructorCall.arguments) {
                    arguments.add(argument.substitute(symbol, substitution));
                }
            }

            return new ConstructorCall(
                    (InductiveType) constructorCall.inductiveType.substitute(symbol, substitution),
                    constructorCall.constructor,
                    arguments
            );
        } else if (this instanceof Fix) {
            Fix fix = (Fix) this;
            List<Definition> definitions = new ArrayList<>();

            for (Definition definition : fix.definitions) {
                Definition newDefinition = new Definition(
                        definition.name,
                        definition.type.substitute(symbol, substitution),
                        definition.definition.substitute(symbol, substitution)
                );
                definitions.add(newDefinition);
            }

            return new Fix(definitions, fix.export);
        } else {
            throw new PouletException("case for ");
        }
    }

    public final Expression makeSymbolsUnique() throws PouletException {
        return makeSymbolsUnique(new HashMap<>());
    }

    final Expression makeSymbolsUnique(Map<Symbol, Symbol> map) throws PouletException {
        return transformSymbols(Symbol::makeUnique, map);
    }

    public final Expression normalizeSymbolNames() throws PouletException {
        int oldNextId = Symbol.nextId;
        Symbol.nextId = 0;
        Expression normalized = transformSymbols(
                symbol -> symbol.rename("").makeUnique(),
                new HashMap<>()
        );
        Symbol.nextId = oldNextId;
        return normalized;
    }

    abstract Expression transformSymbols(Function<Symbol, Symbol> transformer, Map<Symbol, Symbol> map) throws PouletException;

    public abstract <T> T accept(ExpressionVisitor<T> visitor) throws PouletException;
}
