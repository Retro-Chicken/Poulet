package poulet.typing;

import poulet.Util;
import poulet.ast.*;
import poulet.interpreter.Interpreter;
import poulet.value.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class Checker {
    public static void checkKind(Expression type, Environment environment) throws TypeException {
        Expression deduced = deduceType(type, environment);
        if (deduced instanceof Variable) {
            Variable variable = (Variable) deduced;
            String name = variable.symbol.getName();
            if (name.matches("Type\\d+"))
                return;
        }
        throw new TypeException("Type Is Not Valid: Deduced " + deduced);
    }

    public static void checkType(Expression term, Expression type, Environment environment) throws TypeException {
        Expression deduced = deduceType(term, environment);
        // TODO: See if this is sound, check if they're equivalent by beta reduction
        deduced = Interpreter.evaluateExpression(deduced, environment).expression();
        type = Interpreter.evaluateExpression(type, environment).expression();

        // Subtyping check first
        // TODO: Make sure this is allowed
        if (deduced instanceof Variable && type instanceof Variable) {
            String nameDeduced = ((Variable) deduced).symbol.getName();
            String nameType = ((Variable) type).symbol.getName();
            if (nameDeduced.matches("Type\\d+") && nameType.matches("Type\\d+")) {
                int level1 = Integer.parseInt(nameDeduced.substring(4));
                int level2 = Integer.parseInt(nameType.substring(4));
                if (level1 < level2)
                    return;
            }
        }

        if (!deduced.toString().equals(type.toString()))
            throw new TypeException("Type Mismatch:\n" + term + " is of type " + deduced + ", not " + type);
    }

    public static Expression deduceType(Expression term, Environment environment) throws TypeException {
        Expression result = null;
        if (term instanceof Abstraction) {
            Abstraction abstraction = (Abstraction) term;
            Expression abstractionType = Interpreter.evaluateExpression(abstraction.type, environment).expression();

            checkKind(abstractionType, environment);

            Environment newEnvironment = environment.increment();
            Symbol tempSymbol = Util.getUniqueSymbol();
            newEnvironment = newEnvironment.appendType(tempSymbol, abstractionType);
            Expression bodyType = deduceType(substitute(abstraction.body, new Variable(tempSymbol)), newEnvironment);
            result = Interpreter.addIndices(new PiType(tempSymbol, abstractionType, bodyType));
        } else if (term instanceof Variable) {
            Variable variable = (Variable) term;
            Expression variableType = environment.lookUpType(variable.symbol);
            Expression definition = environment.lookUpGlobal(variable.symbol);

            if (variableType != null) {
                result = variableType;
            } else if (definition != null) {
                result = deduceType(definition, environment);
            } else {
                throw new TypeException("Unknown Identifier");
            }
        } else if (term instanceof Application) {
            Application application = (Application) term;
            Expression functionType = deduceType(application.function, environment);
            if (functionType instanceof PiType) {
                PiType piType = (PiType) functionType;
                // TODO: Fix this
                checkType(application.argument, piType.type, environment);
                result = substitute(piType.body, application.argument);
            } else if (functionType instanceof Variable) { // TODO: Double check this sketchy-ness
                Variable variable = (Variable) functionType;
                String name = variable.symbol.getName();

                if (name.matches("Type\\d+")) {
                    if (application.function instanceof PiType) {
                        PiType piType = (PiType) application.function;
                        checkType(application.argument, piType.type, environment);
                        result = new Variable(new Symbol(name));
                    } else if (application.function instanceof Variable) {
                        Variable free = (Variable) application.function;
                        Expression definition = environment.lookUpGlobal(free.symbol);

                        if (definition instanceof PiType) {
                            PiType piType = (PiType) definition;
                            checkType(application.argument, piType.type, environment);
                            result = new Variable(new Symbol(name));
                        }
                    }
                }
            } else {
                throw new TypeException("Application Function is not a Function");
            }
        } else if (term instanceof PiType) {
            PiType piType = (PiType) term;
            Expression typeLevel = deduceType(piType.type, environment);
            Symbol uniqueSymbol = Util.getUniqueSymbol();
            Environment newEnvironment = environment.increment();
            newEnvironment = newEnvironment.appendType(uniqueSymbol, Interpreter.evaluateExpression(piType.type, environment).expression());
            Expression bodyLevel = deduceType(substitute(piType.body, new Variable(uniqueSymbol)), newEnvironment);
            if (typeLevel instanceof Variable && bodyLevel instanceof Variable) {
                Variable typeVar = (Variable) typeLevel;
                Variable bodyVar = (Variable) bodyLevel;
                String typeName = typeVar.symbol.getName();
                String bodyName = bodyVar.symbol.getName();
                if (typeName.matches("Type\\d+") && bodyName.matches("Type\\d+")) {
                    int level1 = Integer.parseInt(typeName.substring(4));
                    int level2 = Integer.parseInt(bodyName.substring(4));
                    result = new Variable(new Symbol("Type" + umax(level1, level2)));
                }
            }
        } else if (term instanceof InductiveType) {
            InductiveType inductiveType = (InductiveType) term;
            TypeDeclaration typeDeclaration = environment.lookUpTypeDeclaration(inductiveType.type);

            if (typeDeclaration == null)
                throw new TypeException("type declaration not found");

            if (inductiveType.parameters.size() != typeDeclaration.parameters.size())
                throw new TypeException("wrong number of parameters");

            Environment newEnvironment = environment;

            for (int i = 0; i < inductiveType.parameters.size(); i++) {
                Expression parameterType = typeDeclaration.parameters.get(i).type;
                Expression parameter = inductiveType.parameters.get(i);
                checkType(parameter, parameterType, environment);
                newEnvironment = newEnvironment.appendGlobal(typeDeclaration.parameters.get(i).symbol, parameter);
            }

            result = Interpreter.evaluateExpression(typeDeclaration.type, newEnvironment).expression();
        } else if (term instanceof ConstructorCall) {
            ConstructorCall constructorCall = (ConstructorCall) term;
            Constructor constructor = environment.lookUpConstructor(constructorCall);

            if (constructor == null)
                throw new TypeException("constructor not found");

            TypeDeclaration typeDeclaration = environment.lookUpTypeDeclaration(constructorCall.inductiveType.type);

            if (typeDeclaration == null)
                throw new TypeException("type declaration not found");

            if (constructorCall.inductiveType.parameters.size() != typeDeclaration.parameters.size())
                throw new TypeException("wrong number of parameters");

            Environment newEnvironment = environment;

            for (int i = 0; i < constructorCall.inductiveType.parameters.size(); i++) {
                Expression parameterType = typeDeclaration.parameters.get(i).type;
                Expression parameter = constructorCall.inductiveType.parameters.get(i);
                checkType(parameter, parameterType, environment);
                newEnvironment = newEnvironment.appendGlobal(typeDeclaration.parameters.get(i).symbol, parameter);
            }

            result = Interpreter.evaluateExpression(constructor.definition, newEnvironment).expression();
        }
        if (result == null) {
            System.out.println("term =" + term);
            throw new TypeException("Type Could not be Deduced");
        }
        return Interpreter.evaluateExpression(result, environment).expression();
    }

    public static Expression substitute(Expression base, Expression substitute) {
        return substitute(base, substitute, 0);
    }

    private static Expression substitute(Expression base, Expression substitute, int i) {
        if (base instanceof Variable) {
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
        } else if (base instanceof Application) {
            Application application = (Application) base;
            Expression function = substitute(application.function, substitute, i);
            Expression argument = substitute(application.argument, substitute, i);
            return new Application(function, argument);
        } else if (base instanceof Abstraction) {
            Abstraction abstraction = (Abstraction) base;
            Expression type = substitute(abstraction.type, substitute, i);
            Expression body = substitute(abstraction.body, substitute, i + 1);
            return new Abstraction(abstraction.symbol, type, body);
        } else if (base instanceof PiType) {
            PiType piType = (PiType) base;
            Expression type = substitute(piType.type, substitute, i);
            Expression body = substitute(piType.body, substitute, i + 1);
            return new PiType(piType.variable, type, body);
        } else if (base instanceof ConstructorCall) {
            ConstructorCall constructorCall = (ConstructorCall) base;

            List<Expression> arguments = null;

            if (constructorCall.isConcrete()) {
                arguments = new ArrayList<>();
                for (Expression argument : constructorCall.arguments) {
                    arguments.add(substitute(argument, substitute));
                }
            }

            return new ConstructorCall(
                    (InductiveType) substitute(constructorCall.inductiveType, substitute, i),
                    constructorCall.constructor,
                    arguments
            );
        } else if (base instanceof InductiveType) {
            InductiveType inductiveType = (InductiveType) base;

            List<Expression> parameters = new ArrayList<>();
            for (Expression parameter : inductiveType.parameters) {
                parameters.add(substitute(parameter, substitute));
            }

            List<Expression> arguments = null;

            if (inductiveType.isConcrete()) {
                arguments = new ArrayList<>();
                for (Expression argument : inductiveType.arguments) {
                    arguments.add(substitute(argument, substitute));
                }
            }

            return new InductiveType(
                    inductiveType.type,
                    inductiveType.parameters,
                    arguments
            );
        }

        return base;
    }

    private static int umax(int level1, int level2) {
        if (level1 <= 2 && level2 <= 2) return 1;
        return Math.max(level1, level2);
    }

    private static <T> boolean allDistinct(Collection<T> collection) {
        return collection.stream().distinct().count() == collection.size();
    }

    private static boolean isSort(Expression expression) {
        if (expression instanceof Variable) {
            Variable variable = (Variable) expression;
            return variable.symbol.getName().matches("Type\\d");
        }
        return false;
    }

    private static boolean isArity(Value value) {
        if (value instanceof VType) {
            return true;
        } else if (value instanceof VPi) {
            VPi vPi = (VPi) value;
            Value dummyArgument = new VNeutral(new NFree(new Symbol("dummy")));
            Value bodyType = vPi.call(dummyArgument);
            return isArity(bodyType);
        }
        return false;
    }

    private static VType getArity(Value value) {
        if (value instanceof VType) {
            return (VType) value;
        } else if (value instanceof VPi) {
            VPi vPi = (VPi) value;
            Value dummyArgument = new VNeutral(new NFree(new Symbol("dummy")));
            Value bodyType = vPi.call(dummyArgument);
            return getArity(bodyType);
        }
        return null;
    }

    private static boolean isTypeOfConstructorOf(Value type, TypeDeclaration typeDeclaration) {
        return false; // TODO
    }

    private static void inductiveDeclarationWellFormed(InductiveDeclaration inductiveDeclaration, Environment environment) throws TypeException {
        List<TypeDeclaration> tds = inductiveDeclaration.typeDeclarations;

        try {
            // k > 0
            assert tds.size() > 0;

            // I_j and c_j all distinct names
            assert allDistinct(tds.stream().map(td -> td.name).collect(Collectors.toList()));
            assert allDistinct(tds.stream().flatMap(td -> td.constructors.stream()).collect(Collectors.toList()));

            // A_j is an arity of sort s_j and I_j ∉ E
            List<VType> arities = new ArrayList<>();
            for (TypeDeclaration td : tds) {
                Value type = Interpreter.evaluateExpression(td.type, new ArrayList<>(), environment);
                assert isArity(type);
                arities.add(getArity(type));
                assert environment.lookUpGlobal(td.name) == null;
            }

            // C_jk is a type of constructor I_j which satisfies the positivity
            // condition for {I_j} and c_ij ∉ E ∪ {I_j}
            for (TypeDeclaration td : tds) {
                for (Constructor c : td.constructors) {

                }
            }
        } catch (AssertionError e) {
            throw new TypeException("typeDeclarations type not well-formed");
        }
    }
}
