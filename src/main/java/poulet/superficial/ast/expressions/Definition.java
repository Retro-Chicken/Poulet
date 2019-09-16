package poulet.superficial.ast.expressions;

import poulet.superficial.Desugar;

public class Definition extends TopLevel {
    public final Symbol name;
    public final Expression type;
    public final Expression definition;

    public Definition(Symbol name, Expression type, Expression definition) {
        this.name = name;
        this.type = type;
        this.definition = definition;
    }

    public Definition(Symbol name, Expression type) {
        this(name, type, null);
    }

    @Override
    public String toString() {
        if (definition == null) {
            return "" + name + " : " + type;
        } else {

            return "" + name + " : " + type + " := " + definition;
        }
    }

    @Override
    public poulet.kernel.ast.Definition project() {
        if(definition != null)
            return new poulet.kernel.ast.Definition(name.project(), Desugar.desugar(type), Desugar.desugar(definition));
        else
            return new poulet.kernel.ast.Definition(name.project(), Desugar.desugar(type));
    }

    @Override
    public Definition makeSymbolsUnique() {
        Expression uniqueDefinition = definition == null ? null : definition.makeSymbolsUnique();
        return new Definition(name, type.makeSymbolsUnique(), uniqueDefinition);
    }
}
