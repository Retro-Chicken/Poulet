package poulet.typing;

import poulet.ast.*;

public class Interpreter {
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
}
