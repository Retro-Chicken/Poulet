package poulet.typing;

import poulet.Util;
import poulet.ast.*;
import poulet.interpreter.Interpreter;

public class Checker {
    public static void checkKind(Context context, Expression type) throws TypeException {
        Expression deduced = deduceType(context, type);
        if(deduced instanceof Variable) {
            Variable variable = (Variable) deduced;
            String name = variable.symbol.getName();
            if(name.matches("Type\\d+"))
                return;
            else { // TODO: This could be an infinite loop
                checkKind(context, deduced);
                return;
            }
        }
        throw new TypeException("Type Is Not Valid: Deduced " + deduced);
    }

    public static void checkType(Context context, Expression term, Expression type) throws TypeException {
        Expression deduced = deduceType(context, term);
        // TODO: See if this is sound, check if they're equivalent by beta reduction
        deduced = Interpreter.evaluateExpression(deduced).expression();
        type = Interpreter.evaluateExpression(type).expression();

        if(!deduced.toString().equals(type.toString()))
            throw new TypeException("Type Mismatch:\n" + term + " is of type " + deduced + ", not " + type);
    }

    public static Expression deduceType(Context context, Expression term) throws TypeException {
        Expression result = null;
        if (term instanceof Abstraction) {
            Abstraction abstraction = (Abstraction) term;
            Expression abstractionType = Interpreter.evaluateExpression(abstraction.type).expression();

            checkKind(context, abstractionType);

            Context newContext = context.increment();
            Symbol tempSymbol = Util.getUniqueSymbol();
            newContext = newContext.append(tempSymbol, abstractionType);
            Expression bodyType = deduceType(newContext, substitute(abstraction.body, new Variable(tempSymbol)));
            result = Interpreter.addIndices(new PiType(tempSymbol, abstractionType, bodyType));
        } else if (term instanceof Variable) {
            Variable variable = (Variable) term;
            Expression variableType = context.lookUp(variable.symbol);

            if (variableType == null)
                throw new TypeException("Unknown Identifier");

            result = variableType;
        } else if (term instanceof Application) {
            Application application = (Application) term;
            Expression functionType = deduceType(context, application.function);
            if (functionType instanceof PiType) {
                PiType piType = (PiType) functionType;
                // TODO: Fix this
                checkType(context, application.argument, piType.type);
                result = substitute(piType.body, application.argument);
            } else if(functionType instanceof Variable) { // TODO: Double check this sketchy-ness
                Variable variable = (Variable) functionType;
                String name = variable.symbol.getName();
                if(name.matches("Type\\d+")) {
                    if(application.function instanceof PiType) {
                        PiType piType = (PiType) application.function;
                        checkType(context, application.argument, piType.type);
                        result = new Variable(new Symbol(name));
                    }
                }
            } else
                throw new TypeException("Application Function is not a Function");
        } else if (term instanceof PiType) {
            PiType piType = (PiType) term;
            Expression typeLevel = deduceType(context, piType.type);
            Symbol uniqueSymbol = Util.getUniqueSymbol();
            Context newContext = context.increment();
            newContext = newContext.append(uniqueSymbol, Interpreter.evaluateExpression(piType.type).expression());
            Expression bodyLevel = deduceType(newContext, substitute(piType.body, new Variable(uniqueSymbol)));
            if(typeLevel instanceof Variable && bodyLevel instanceof Variable) {
                Variable typeVar = (Variable) typeLevel;
                Variable bodyVar = (Variable) bodyLevel;
                String typeName = typeVar.symbol.getName();
                String bodyName = bodyVar.symbol.getName();
                if(typeName.matches("Type\\d+") && bodyName.matches("Type\\d+")) {
                    int level1 = Integer.parseInt(typeName.substring(4));
                    int level2 = Integer.parseInt(bodyName.substring(4));
                    result = new Variable(new Symbol("Type" + umax(level1, level2)));
                }
            }
        }
        if(result == null)
            throw new TypeException("Type Could not be Deduced");
        return Interpreter.evaluateExpression(result).expression();
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

    private static int umax(int level1, int level2) {
        if(level1 <= 2 && level2 <= 2) return 1;
        return Math.max(level1, level2);
    }
}
