package poulet.typing;

import poulet.ast.*;

public class Checker {
    public static PiType deduceType(Expression expression, Context context) {
        if(expression instanceof Abstraction) {
            Abstraction abstraction = (Abstraction) expression;

        } else if(expression instanceof Application) {
            Application application = (Application) expression;
        } else if(expression instanceof Variable) {
            Variable variable = (Variable) expression;
        }
        return null;
    }

    public static Expression getType(Expression expression, Context context) throws Exception {
        if(expression instanceof Abstraction) {
            Abstraction abstraction = (Abstraction) expression;
            context = context.append(abstraction.symbol, abstraction.type);
            context = context.append(abstraction.symbol, abstraction.type);
            Expression userType = getType(abstraction.body, context);
            if(!deduceType(abstraction.body, context).equals(userType))
                throw new Exception("Type Mismatch!");
            return new PiType(abstraction.symbol, abstraction.type, getType(abstraction.body, context));
        } else if(expression instanceof Application) {
            Application application = (Application) expression;
            Expression userFunctionType = getType(application.function, context);
            if(!deduceType(application.function, context).equals(userFunctionType))
                throw new Exception("Type Mismatch!");
            if(!(userFunctionType instanceof PiType))
                throw new Exception("Type Mismatch!");
            PiType userFunctionPiType = (PiType) userFunctionType;
            Expression userArgumentType = getType(application.argument, context);
            if(!(userArgumentType instanceof PiType))
                throw new Exception("Type Mismatch!");
            PiType userArgumentPiType = (PiType) userArgumentType;
            if(!deduceType(application.argument, context).equals(userArgumentType))
                throw new Exception("Type Mismatch!");
            return new PiType(userArgumentPiType.variable, userArgumentPiType.type, userFunctionPiType.body);
        } else if(expression instanceof Variable) {
            Variable variable = (Variable) expression;
            Expression userType = context.lookUp(variable.symbol);
            return userType;
        }
        return null;
    }

    public static void checkKind(Context context, Expression type) throws TypeException {
        if (type instanceof Variable) {
            Expression kindOfType = context.lookUp(((Variable) type).symbol);
            if (kindOfType instanceof Variable) {
                if (((Variable) kindOfType).symbol.equals(new Symbol("*"))) {
                    return;
                }
            }
            throw new TypeException("unknown identifier");
        } else if (type instanceof PiType) {
            PiType piType = (PiType) type;
            checkKind(context, piType.type);
            checkKind(context, piType.body);
        }
    }

    public static void checkType(Context context, Expression term, Expression type) throws TypeException {
        if (term instanceof Abstraction && type instanceof PiType) {
            Abstraction abstraction = (Abstraction) term;
            PiType piType = (PiType) type;
            Context newContext = context.increment();
            newContext = newContext.append(new Symbol(0), abstraction.type);
            checkType(newContext, abstraction.body, piType.body);
        } else if (term instanceof Variable) {
            Variable variable = (Variable) term;
            Expression variableType = context.lookUp(variable.symbol);

            if (variableType == null) {
                throw new TypeException("unknown identifier");
            }

            if (variableType.equals(type)) {
                return;
            }

            throw new TypeException("type mismatch");
        } else if (term instanceof Application) {
            Application application = (Application) term;
            Expression argumentType = deduceType(context, application.argument);
            Expression requiredFunctionType = new PiType(
                    new Symbol("_"),
                    argumentType,
                    type
            );
            checkType(context, application.function, requiredFunctionType);
        } else {
            throw new TypeException("type mismatch");
        }
    }

    public static Expression deduceType(Context context, Expression term) throws TypeException {
        if (term instanceof Abstraction) {
            Abstraction abstraction = (Abstraction) term;
            Context newContext = context.increment();
            newContext = newContext.append(new Symbol(0), abstraction.type);
            Expression bodyType = deduceType(newContext, abstraction.body);
            return new PiType(new Symbol("_"), abstraction.type, bodyType);
        } else if (term instanceof Variable) {
            Variable variable = (Variable) term;
            Expression variableType = context.lookUp(variable.symbol);

            if (variableType == null) {
                throw new TypeException("unknown identifier");
            }

            return variableType;
        } else if (term instanceof Application) {
            Application application = (Application) term;
            Expression bodyType = deduceType(context, application.function);
            if (bodyType instanceof PiType) {
                PiType piType = (PiType) bodyType;
                checkType(context, application.argument, piType.type);
                return piType.body;
            }
        }

        return null;
    }
}
