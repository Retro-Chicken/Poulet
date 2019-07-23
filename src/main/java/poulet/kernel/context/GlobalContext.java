package poulet.kernel.context;

import poulet.kernel.ast.*;
import poulet.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

public class GlobalContext extends Context {
    private final Map<Symbol, TypeDeclaration> typeDeclarations;

    public GlobalContext() {
        typeDeclarations = new HashMap<>();
    }

    public GlobalContext(Map<Symbol, Expression> types, Map<Symbol, Expression> scope, Map<Symbol, TypeDeclaration> typeDeclarations) {
        super(types, scope);
        this.typeDeclarations = typeDeclarations;
    }

    // copy constructor
    public GlobalContext(GlobalContext globalContext) {
        super(globalContext);
        typeDeclarations = new HashMap<>(globalContext.typeDeclarations);
    }

    public void declareInductive(InductiveDeclaration inductiveDeclaration) {
        for (TypeDeclaration typeDeclaration : inductiveDeclaration.typeDeclarations) {
            typeDeclarations.put(
                    typeDeclaration.name,
                    typeDeclaration
            );
        }
    }

    public TypeDeclaration getTypeDeclaration(Symbol symbol) {
        return typeDeclarations.getOrDefault(symbol, null);
    }

    public TypeDeclaration.Constructor getConstructor(ConstructorCall constructorCall) {
        TypeDeclaration typeDeclaration = getTypeDeclaration(constructorCall.inductiveType);

        if (typeDeclaration == null) {
            return null;
        }

        for (TypeDeclaration.Constructor c : typeDeclaration.constructors) {
            if (c.name.equals(constructorCall.constructor)) {
                return c;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return StringUtil.mapToStringWithNewlines(Map.ofEntries(
                Map.entry("assumptions", assumptions),
                Map.entry("definitions", definitions),
                Map.entry("typeDeclarations", typeDeclarations)
        ));
    }
}
