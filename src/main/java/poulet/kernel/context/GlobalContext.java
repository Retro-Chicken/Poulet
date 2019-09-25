package poulet.kernel.context;

import poulet.PouletException;
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
        TypeDeclaration result = typeDeclarations.getOrDefault(symbol, null);
        if(result == null)
            throw new PouletException("Cannot find TypeDeclaration under name " + symbol);
        return result;
    }

    public TypeDeclaration.Constructor getConstructor(Symbol inductiveType, Symbol constructor) {
        TypeDeclaration typeDeclaration = getTypeDeclaration(inductiveType);

        for (TypeDeclaration.Constructor c : typeDeclaration.constructors) {
            if (c.name.equals(constructor)) {
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
