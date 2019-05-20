package poulet.typing;

import poulet.Util;
import poulet.ast.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class Environment {
    public final Map<Symbol, Expression> types;
    public final Map<Symbol, Expression> scope;
    public final Map<Symbol, TypeDeclaration> typeDeclarations;

    public Environment() {
        this.types = new HashMap<>();
        this.scope = new HashMap<>();
        this.typeDeclarations = new HashMap<>();
    }

    public Environment(Map<Symbol, Expression> types, Map<Symbol, Expression> scope, Map<Symbol, TypeDeclaration> typeDeclarations) {
        this.types = types;
        this.scope = scope;
        this.typeDeclarations = typeDeclarations;
    }

    public Environment appendType(Symbol symbol, Expression expression) {
        Map<Symbol, Expression> newTypes = new HashMap<>(types);
        newTypes.put(symbol, expression);
        return new Environment(newTypes, scope, typeDeclarations);
    }

    public Environment appendScope(Symbol symbol, Expression expression) {
        Map<Symbol, Expression> newScope = new HashMap<>(this.scope);
        newScope.put(symbol, expression);
        return new Environment(types, newScope, typeDeclarations);
    }

    public Environment appendInductive(InductiveDeclaration inductiveDeclaration) {
        Map<Symbol, TypeDeclaration> newInductive = new HashMap<>(typeDeclarations);

        for (TypeDeclaration td : inductiveDeclaration.typeDeclarations) {
            newInductive.put(td.name, td.addInductiveDeclaration(inductiveDeclaration));
        }

        return new Environment(types, scope, newInductive);
    }

    public Expression lookUpType(Symbol symbol) {
        if (symbol.name.matches("Type\\d+")) {
            int level = Integer.parseInt(symbol.name.substring(4));
            return new Variable(new Symbol("Type" + (level + 1)));
        }
        return types.getOrDefault(symbol, null);
    }

    public Expression lookUpScope(Symbol symbol) {
        return scope.getOrDefault(symbol, null);
    }

    public TypeDeclaration lookUpTypeDeclaration(Symbol symbol) {
        return typeDeclarations.getOrDefault(symbol, null);
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
        return Util.mapToStringWithNewlines(Map.of(
                "types", types,
                "scope", scope,
                "typeDeclarations", typeDeclarations
        ));
    }
}
