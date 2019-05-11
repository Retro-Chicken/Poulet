package poulet.typing;

import poulet.ast.*;
import poulet.interpreter.Interpreter;
import poulet.temp.TempSymbol;
import poulet.value.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Checker {
    /*
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
    }*/

    public static void checkKind(EContext context, Expression type) throws TypeException {
        return;
        /*
        if (type instanceof Variable) {
            Expression kindOfType = context.lookUp(((Variable) type).symbol);
            if (kindOfType instanceof Variable) {
                if(((Variable) kindOfType).symbol.getName().matches("Type\\d+")) {
                    return;
                }
            }
            throw new TypeException("unknown identifier");
        } else if (type instanceof PiType) {
            PiType piType = (PiType) type;
            checkKind(context, piType.type);
            checkKind(context, piType.body);
        }*/
    }

    public static void checkType(EContext context, Expression term, Expression type) throws TypeException {
        /*
        checkKind(context, type);
        if (term instanceof Abstraction && type instanceof PiType) {
            Abstraction abstraction = (Abstraction) term;
            PiType piType = (PiType) type;
            EContext newContext = context.increment();
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
            System.out.println("Input: " + variable);
            System.out.println("Look-Up: " + variableType);
            System.out.println("Type: " + type);

            throw new TypeException("type mismatch");
        } else if (term instanceof Application) {
            Application application = (Application) term;
            Expression argumentType = deduceType(context, application.argument);
            Expression requiredFunctionType = new PiType(
                    null,
                    argumentType,
                    type
            );
            checkType(context, application.function, requiredFunctionType);
        } else if (term instanceof PiType && type.equals(new Variable(new Symbol("Type1")))) {
            return;
        } else {
            throw new TypeException("type mismatch " + term + ", " + type);
        }*/
        Expression deduced = deduceType(context, term);
        deduced = Interpreter.addIndices(deduced);
        if(!deduced.toString().equals(type.toString())) {
            throw new TypeException("Type Mismatch:\n" + term + " is of type " + deduced + ", not " + type);
        }
    }

    public static Expression deduceType(EContext context, Expression term) throws TypeException {
        if (term instanceof Abstraction) {
            Abstraction abstraction = (Abstraction) term;
            EContext newContext = context.increment();

            Symbol tempSymbol = new Symbol("" + new Random().nextInt(1000));

            //newContext = newContext.append(new Symbol(0), abstraction.type);
            newContext = newContext.append(tempSymbol, abstraction.type);
            //System.out.println(substitute(abstraction.body, new Variable(new Symbol("TEST" + new Random().nextInt(1000)))));
            Expression bodyType = deduceType(newContext, substitute(abstraction.body, new Variable(tempSymbol)));
            return new PiType(tempSymbol, abstraction.type, bodyType);
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
                checkType(context, application.argument, piType.type);
                /*
                if(application.argument instanceof Variable) {
                    Symbol uniqueSymbol = new Symbol("" + new Random().nextInt(1000));
                    EContext newContext = context.increment();
                    newContext = newContext.append(uniqueSymbol, );
                    functionType = deduceType(newContext, substitute(application.function, new Variable(uniqueSymbol)));
                }*/
                if(application.function instanceof Abstraction) {
                    Abstraction abstraction = (Abstraction) application.function;
                    Expression bodyType = deduceType(context, substitute(abstraction.body, application.argument));
                    //System.out.println("Body Type: " + bodyType);
                    return bodyType;
                }
                return substitute(piType.body, application.argument);
                //return piType.body;
            }
        } else if (term instanceof PiType) {
            //PiType piType = (PiType) term;
            return new Variable(new Symbol("Type1"));
        }

        return null;
    }

    private static VNeutral vFree(Name name) {
        return new VNeutral(new NFree(name));
    }

    public static void checkKind(Expression expression, Context context) {
        return;
    }

    public static Value deduceType(Expression expression, Context context) throws TypeException {
        return deduceType(expression, context, new ArrayList<>(), 0);
    }

    private static Value deduceType(Expression expression, Context context, List<Value> bound, int i) throws TypeException {
        if(expression instanceof Variable) {
            Variable variable = (Variable) expression;
            if (variable.symbol.isFree()) {
                String name = variable.symbol.getName();
                if (name.matches("Type\\d+")) {
                    int level = Integer.parseInt(name.substring(4));
                    return new VType(level);
                } else {
                    Value result = context.lookUp(variable.symbol);
                    if(result == null)
                        throw new TypeException("Unknown Identifier");
                    return result;
                }
            }
        } else if(expression instanceof PiType) {
            // TODO: Confirm PiType is valid
            return new VType(1);
        } else if(expression instanceof Application) {
            Application application = (Application) expression;
            Value type = deduceType(application.function, context, bound, i);
            if(type instanceof VPi) {
                VPi vPi = (VPi) type;
                checkType(application.argument, vPi.type, context, bound, i);
                return vPi.call(Interpreter.evaluateExpression(application.argument, bound));
            } else
                throw new TypeException("Illegal Application");
        } else if(expression instanceof Annotation) {
            Annotation annotation = (Annotation) expression;
            checkKind(annotation.type, context);
            Value type = Interpreter.evaluateExpression(annotation.type);
            checkType(annotation.expression, type, context, bound, i);
            return type;
        } else if(expression instanceof Abstraction) {
            Abstraction abstraction = (Abstraction) expression;
            Context newContext = context.increment();
            // TODO: Fix extreme sketchy-ness
            //Symbol uniqueSymbol = new Symbol("bound" + bound.size(), "" + new Random().nextInt(10000));
            //Quote uniqueSymbol = new Quote(bound.size());
            TempSymbol uniqueSymbol = new TempSymbol(bound.size());
            // TODO: Need to pass bound variables from abstraction when doing types
            newContext = newContext.append(uniqueSymbol, Interpreter.evaluateExpression(abstraction.type, bound));

            List<Value> newBound = new ArrayList<>();
            newBound.add(new VNeutral(new NFree(uniqueSymbol)));
            newBound.addAll(bound);

            Value bodyType = deduceType(substitute(abstraction.body, new Variable(uniqueSymbol)), newContext, newBound, i);
            //PiType type = new PiType(null, abstraction.type, bodyType.expression());
            return new VPi(Interpreter.evaluateExpression(abstraction.type, bound), argument -> bodyType);
            //return Interpreter.evaluateExpression(type, bound);
        }

        return null;
    }


    public static void checkType(Expression expression, Value type, Context context) throws TypeException {
        checkType(expression, type, context, new ArrayList<>(), 0);
    }

    private static void checkType(Expression expression, Value type, Context context, List<Value> bound, int i) throws TypeException {
        if(expression instanceof Abstraction) {
            if(type instanceof VPi) {
                Abstraction abstraction = (Abstraction) expression;
                VPi vPi = (VPi) type;
                Context newContext = context.increment();
                // TODO: Fix extreme sketchy-ness
                //Symbol uniqueSymbol = new Symbol("" + new Random().nextInt(10000));
                TempSymbol uniqueSymbol = new TempSymbol(i);

                List<Value> newBound = new ArrayList<>();
                newBound.add(vFree(uniqueSymbol));
                newBound.addAll(bound);

                newContext = newContext.append(uniqueSymbol, vPi.type);
                checkType(substitute(abstraction.body, new Variable(uniqueSymbol)),
                        vPi.call(vFree(uniqueSymbol)),
                        newContext, newBound, i + 1);
                return;
            } else
                throw new TypeException("Abstraction Cannot Have Type " + type);
        } else if (expression instanceof Variable) {
            Variable variable = (Variable) expression;
            Value deducedType = deduceType(variable, context, bound, i);
            if(!deducedType.equals(type))
                throw new TypeException("Type Mismatch");
            return;
        } else if (expression instanceof Application) {
            Application application = (Application) expression;
            Value deducedType = deduceType(application, context, bound, i);
            if(!deducedType.equals(type))
                throw new TypeException("Type Mismatch");
            return;
        } else if(expression instanceof PiType) {
            PiType piType = (PiType) expression;
            Value deducedType = deduceType(piType, context, bound, i);
            if(!deducedType.equals(type))
                throw new TypeException("Type Mismatch");
            return;
        } else if(expression instanceof Annotation) {
            Annotation annotation = (Annotation) expression;
            Value deducedType = deduceType(annotation, context, bound, i);
            if(!deducedType.equals(type))
                throw new TypeException("Type Mismatch");
            return;
        } else if(expression == null) {
            throw new TypeException("Checking Type of null");
        }

        throw new TypeException("Cannot Check Type of " + expression.getClass().getSimpleName());
    }

    private static Expression substitute(Expression base, Expression substitute) {
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
        } else if(base instanceof Annotation) {
            Annotation annotation = (Annotation) base;
            Expression expression = substitute(annotation.expression, substitute, i);
            Expression type = substitute(annotation.type, substitute, i);
            return new Annotation(expression, type);
        }

        return base;
    }
}
