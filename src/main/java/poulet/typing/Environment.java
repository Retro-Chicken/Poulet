package poulet.typing;

import poulet.ast.*;
import poulet.value.Name;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    public final Map<Name, Expression> types;
    public final Map<Name, Expression> globals;
    public final Map<Name, TypeDeclaration> typeDeclarations;

    public Environment() {
        this.types = new HashMap<>();
        this.globals = new HashMap<>();
        this.typeDeclarations = new HashMap<>();
    }

    public Environment(Map<Name, Expression> types, Map<Name, Expression> globals, Map<Name, TypeDeclaration> typeDeclarations) {
        this.types = types;
        this.globals = globals;
        this.typeDeclarations = typeDeclarations;
    }

    public Environment appendType(Name name, Expression type) {
        Map<Name, Expression> newTypes = new HashMap<>(types);
        newTypes.put(name, type);
        return new Environment(newTypes, globals, typeDeclarations);
    }

    public Environment appendGlobal(Name name, Expression global) {
        Map<Name, Expression> newGlobals = new HashMap<>(globals);
        newGlobals.put(name, global);
        return new Environment(types, newGlobals, typeDeclarations);
    }

    public Environment appendInductive(InductiveDeclaration inductiveDeclaration) {
        Map<Name, TypeDeclaration> newInductive = new HashMap<>(typeDeclarations);

        for (TypeDeclaration td : inductiveDeclaration.typeDeclarations) {
            newInductive.put(td.name, td.addInductiveDeclaration(inductiveDeclaration));
        }

        return new Environment(types, globals, newInductive);
    }

    /**
     * increment all bound variable indices,
     * used when stepping into an abstraction
     */
    public Environment increment() {
        Map<Name, Expression> newTypes = new HashMap<>();
        for (Map.Entry<Name, Expression> entry : types.entrySet()) {
            newTypes.put(entry.getKey().increment(), entry.getValue());
        }
        return new Environment(newTypes, globals, typeDeclarations);
    }

    public Expression lookUpType(Name name) {
        if(name instanceof Symbol) {
            Symbol symbol = (Symbol) name;
            if (symbol.getName() != null && symbol.getName().matches("Type\\d+")) {
                int level = Integer.parseInt(symbol.getName().substring(4));
                return new Variable(new Symbol("Type" + (level + 1)));
            }
        }
        return types.getOrDefault(name, null);
    }

    public Expression lookUpGlobal(Name name) {
        return globals.getOrDefault(name, null);
    }

    public TypeDeclaration lookUpTypeDeclaration(Name name) {
        return typeDeclarations.getOrDefault(name, null);
    }

    public Constructor lookUpConstructor(ConstructorCall constructorCall) {
        TypeDeclaration td = lookUpTypeDeclaration(constructorCall.inductiveType.type);

        if (td == null)
            return null;

        for (Constructor c : td.constructors) {
            if (c.name.equals(constructorCall.constructor)) {
                return c;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return String.format("{\ntypes: %s\nglobals: %s\ntypeDeclarations: %s\n}", types, globals, typeDeclarations);
    }
}
