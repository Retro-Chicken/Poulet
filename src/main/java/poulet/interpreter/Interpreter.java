package poulet.interpreter;

import poulet.ast.*;

import java.util.*;

public class Interpreter {
    private Program program;

    public Interpreter(Program program) {
        this.program = program;
    }

    public static List<Expression> getExpressions(Program program) {
        ArrayList<Expression> expressions = new ArrayList<>();
        for (TopLevel topLevel : program.program) {
            if (topLevel instanceof Definition) {
                Definition definition = (Definition) topLevel;
                expressions.add(definition.definition);
            }
        }
        return expressions;
    }

    public static Expression evaluateExpression(Expression expression) {
        if (isValue(expression))
            return expression;

        if (expression instanceof Application) {
            Application application = (Application) expression;
            Expression function = evaluateExpression(application.function);
            Expression argument = evaluateExpression(application.argument);

            if (function instanceof Abstraction && isValue(argument)) {
                Abstraction abstraction = (Abstraction) function;
                return substitute(abstraction.body, abstraction.symbol, argument);
            } else {
                return new Application(function, argument);
            }

        }

        return null;
    }

    private static Expression substitute(Expression expression, Symbol symbol, Expression substitution) {
        if (expression instanceof Variable) {
            Variable variable = (Variable) expression;
            if (variable.symbol.equals(symbol)) {
                return substitution;
            } else {
                return expression;
            }
        } else if (expression instanceof Application) {
            Application application = (Application) expression;
            Expression function = substitute(application.function, symbol, substitution);
            Expression argument = substitute(application.argument, symbol, substitution);
            return new Application(function, argument);
        } else if (expression instanceof Abstraction) {
            Abstraction abstraction = (Abstraction) expression;
            if (abstraction.symbol.equals(symbol)) {
                return expression;
            } else {
                Expression body = substitute(abstraction.body, symbol, substitution);
                return new Abstraction(abstraction.symbol, abstraction.type, body);
            }
        }

        return null;
    }

    public static Expression addSymbolIDs(Expression expression) {
        if (expression instanceof Abstraction) {
            Abstraction abstraction = (Abstraction) expression;
            Symbol symbol = abstraction.symbol.bind();
            Expression body = tag(abstraction.body, symbol);
            body = addSymbolIDs(body);
            return new Abstraction(symbol, abstraction.type, body);
        } else if (expression instanceof Application) {
            Application application = (Application) expression;
            Expression function = addSymbolIDs(application.function);
            Expression argument = addSymbolIDs(application.argument);
            return new Application(function, argument);
        }

        return expression;
    }

    private static Expression tag(Expression expression, Symbol bound) {
        if (expression instanceof Variable) {
            Variable variable = (Variable) expression;

            if (variable.symbol.name.equals(bound.name)) {
                return new Variable(variable.symbol.copyID(bound));
            }

            return variable;
        } else if (expression instanceof Abstraction) {
            Abstraction abstraction = (Abstraction) expression;
            Expression body = tag(abstraction.body, bound);
            return new Abstraction(abstraction.symbol, abstraction.type, body);
        } else if (expression instanceof Application) {
            Application application = (Application) expression;
            Expression function = tag(application.function, bound);
            Expression argument = tag(application.argument, bound);
            return new Application(function, argument);
        }

        return null;
    }

    private static Set<Symbol> getFreeVariables(Expression expression) {
        if (expression instanceof Variable) {
            Variable variable = (Variable) expression;
            return new HashSet<Symbol>(Arrays.asList(variable.symbol));
        } else if (expression instanceof Abstraction) {
            Abstraction abstraction = (Abstraction) expression;
            Set<Symbol> bodyFree = getFreeVariables(abstraction.body);
            bodyFree.remove(abstraction.symbol);
            return bodyFree;
        } else if (expression instanceof Application) {
            Application application = (Application) expression;
            Set<Symbol> functionFree = getFreeVariables(application.function);
            Set<Symbol> argumentFree = getFreeVariables(application.argument);
            functionFree.addAll(argumentFree);
            return functionFree;
        }

        return null;
    }

    private static boolean isValue(Expression expression) {
        return expression instanceof Abstraction || expression instanceof Variable;
    }
}
