package poulet.kernel.context;

import poulet.kernel.ast.*;
import poulet.util.StringUtil;

import java.util.Map;

public class LocalContext extends Context {
    public final GlobalContext globalContext;

    // new local context with given global context
    public LocalContext(GlobalContext globalContext) {
        // create copy of global context
        this.globalContext = new GlobalContext(globalContext);
    }

    // copy constructor
    public LocalContext(LocalContext localContext) {
        super(localContext);
        this.globalContext = localContext.globalContext;
    }

    public Expression getAssumption(Symbol symbol) {
        // check local context first
        Expression type = super.getAssumption(symbol);

        if (type == null) {
            // fall back to global context if not in local context
            return globalContext.getAssumption(symbol);
        } else {
            return type;
        }
    }

    @Override
    public Expression getDefinition(Symbol symbol) {
        // check local context first
        Expression type = super.getDefinition(symbol);

        if (type == null) {
            // fall back to global context if not in local context
            return globalContext.getDefinition(symbol);
        } else {
            return type;
        }
    }

    public void declareInductive(InductiveDeclaration inductiveDeclaration) {
        globalContext.declareInductive(inductiveDeclaration);
    }

    public TypeDeclaration getTypeDeclaration(Symbol symbol) {
        return globalContext.getTypeDeclaration(symbol);
    }

    public TypeDeclaration.Constructor getConstructor(ConstructorCall constructorCall) {
        return globalContext.getConstructor(constructorCall);
    }

    @Override
    public String toString() {
        return StringUtil.mapToStringWithNewlines(Map.ofEntries(
                Map.entry("global", globalContext),
                Map.entry("local", super.toString())
        ));
    }
}
