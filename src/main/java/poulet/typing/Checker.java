package poulet.typing;

import poulet.Util;
import poulet.ast.*;
import poulet.interpreter.Interpreter;

public class Checker {
    public static void checkKind(Context context, Expression type) throws TypeException {
        if(type instanceof PiType) {
            PiType piType = (PiType) type;
            checkKind(context, piType.type);
            checkKind(context, piType.body);
            return;
        } else if(type instanceof Variable) {
            Variable variable = (Variable) type;
            if(variable.symbol.isFree()) {
                Expression deducedType = deduceType(context, variable);
                if(deducedType instanceof Variable) {
                    Variable variableType = (Variable) deducedType;
                    String name = variableType.symbol.getName();
                    if(name.matches("Type\\d+"))
                        return;
                }
                String name = variable.symbol.getName();
                if(name.matches("Type\\d+"))
                    return;
            } else
                return;
        }
        throw new TypeException("Unrecognized Type " + type);
    }

    public static void checkType(Context context, Expression term, Expression type) throws TypeException {
        Expression deduced = deduceType(context, term);
        if(!deduced.toString().equals(type.toString()))
            throw new TypeException("Type Mismatch:\n" + term + " is of type " + deduced + ", not " + type);
    }

    public static Expression deduceType(Context context, Expression term) throws TypeException {
        if (term instanceof Abstraction) {
            Abstraction abstraction = (Abstraction) term;

            checkKind(context, abstraction.type);

            Context newContext = context.increment();
            Symbol tempSymbol = Util.getUniqueSymbol();
            newContext = newContext.append(tempSymbol, abstraction.type);
            Expression bodyType = deduceType(newContext, substitute(abstraction.body, new Variable(tempSymbol)));
            return Interpreter.addIndices(new PiType(tempSymbol, abstraction.type, bodyType));
        } else if (term instanceof Variable) {
            Variable variable = (Variable) term;
            Expression variableType = context.lookUp(variable.symbol);

            if (variableType == null) {
                throw new TypeException("unknown identifier");
            }

            return variableType;
        } else if (term instanceof Application) {
            Application application = (Application) term;
            Expression functionType = deduceType(context, application.function);
            if (functionType instanceof PiType) {
                PiType piType = (PiType) functionType;
                // TODO: Fix this
                checkType(context, application.argument, piType.type);
                return substitute(piType.body, application.argument);
            }
        } else if (term instanceof PiType) {
            checkKind(context, term);
            return new Variable(new Symbol("Type1"));
        }

        return null;
    }

    public static Expression substitute(Expression base, Expression substitute) {
        return substitute(base, substitute, 0);
    }

    private static Expression substitute(Expression base, Expression substitute, int i) {
        if(base instanceof Variable) {
            Variable variable = (Variable) base;

            if (variable.symbol.isFree()) {
                return variable;
            } else {
                int index = variable.symbol.getIndex();
                if (index == i)
                    return substitute;
                else
                    return base;
            }
        } else if(base instanceof Application) {
            Application application = (Application) base;
            Expression function = substitute(application.function, substitute, i);
            Expression argument = substitute(application.argument, substitute, i);
            return new Application(function, argument);
        } else if(base instanceof Abstraction) {
            Abstraction abstraction = (Abstraction) base;
            Expression type = substitute(abstraction.type, substitute, i);
            Expression body = substitute(abstraction.body, substitute, i + 1);
            return new Abstraction(abstraction.symbol, type, body);
        } else if(base instanceof PiType) {
            PiType piType = (PiType) base;
            Expression type = substitute(piType.type, substitute, i);
            Expression body = substitute(piType.body, substitute, i + 1);
            return new PiType(piType.variable, type, body);
        }

        return base;
    }
}
