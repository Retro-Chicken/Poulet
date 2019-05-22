package poulet.ast;

import poulet.Util;
import poulet.exceptions.PouletException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Fix extends Expression {
    public final List<Definition> definitions;
    public final Symbol export;

    public Fix(List<Definition> definitions, Symbol export) {
        this.definitions = definitions;
        this.export = export;
    }

    @Override
    public String toString() {
        String items = definitions.stream().map(Definition::toString).collect(Collectors.joining("\n"));
        return "fix {\n" + Util.indent(items, 2) + "\n}." + export;
    }

    @Override
    Fix makeSymbolsUnique(Map<Symbol, Symbol> map) throws PouletException {
        Symbol newExport = null;
        Map<Symbol, Symbol> newMap = new HashMap<>(map);

        for (Definition definition : definitions) {
            Symbol name = definition.name.makeUnique();
            newMap.put(definition.name, name);

            if (definition.name.equals(export)) {
                newExport = name;
            }
        }

        if (newExport == null) {
            throw new PouletException("function " + export + " not defined in " + this);
        }

        List<Definition> newDefinitions = new ArrayList<>();

        for (Definition definition : definitions) {
            Definition newDefinition = new Definition(
                    newMap.get(definition.name),
                    definition.type.makeSymbolsUnique(newMap),
                    definition.definition.makeSymbolsUnique(newMap)
            );
            newDefinitions.add(newDefinition);
        }

        return new Fix(newDefinitions, newExport);
    }

    public <T> T accept(ExpressionVisitor<T> visitor) throws PouletException {
        return visitor.visit(this);
    }
}
