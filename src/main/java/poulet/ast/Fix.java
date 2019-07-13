package poulet.ast;

import poulet.typing.Environment;
import poulet.util.StringUtil;
import poulet.exceptions.PouletException;
import poulet.util.ExpressionVisitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Fix extends Expression {
    public final List<Definition> definitions;
    public final Symbol export;

    public Fix(List<Definition> definitions, Symbol export, Environment environment) throws PouletException {
        super(environment);
        this.export = export;
        // Add recursive function types and definitions to environment for definition bodies
        Environment innerEnvironment = environment;
        if(innerEnvironment != null) {
            for (Definition definition : definitions) {
                innerEnvironment = innerEnvironment.appendType(definition.name, definition.type);
                Fix newFix = new Fix(definitions, definition.name);
                innerEnvironment = innerEnvironment.appendScope(definition.name, newFix);
            }
        }
        List<Definition> contextDefinitions = new ArrayList<>();
        for(Definition definition : definitions)
            contextDefinitions.add(new Definition(definition.name, definition.type.context(environment), definition.definition.context(innerEnvironment)));
        this.definitions = contextDefinitions;
    }

    public Fix(List<Definition> definitions, Symbol export) throws PouletException {
        this(definitions, export, null);
    }

    public Definition getExported() throws PouletException {
        for (Definition definition : definitions) {
            if (definition.name.equals(export)) {
                return definition;
            }
        }

        throw new PouletException("symbol " + export + " not defined in " + this);
    }

    @Override
    public String toString() {
        String items = definitions.stream().map(Definition::toString).collect(Collectors.joining("\n"));
        return "fix {\n" + StringUtil.indent(items, 2) + "\n}." + export;
    }

    @Override
    Fix transformSymbols(Function<Symbol, Symbol> transformer, Map<Symbol, Symbol> map) throws PouletException {
        Symbol newExport = null;
        Map<Symbol, Symbol> newMap = new HashMap<>(map);

        for (Definition definition : definitions) {
            Symbol newName = transformer.apply(definition.name);
            newMap.put(definition.name, newName);

            if (definition.name.equals(export)) {
                newExport = newName;
            }
        }

        if (newExport == null) {
            throw new PouletException("function " + export + " not defined in " + this);
        }

        List<Definition> newDefinitions = new ArrayList<>();

        for (Definition definition : definitions) {
            Definition newDefinition = new Definition(
                    newMap.get(definition.name),
                    definition.type.transformSymbols(transformer, map),
                    definition.definition.transformSymbols(transformer, newMap)
            );
            newDefinitions.add(newDefinition);
        }

        return new Fix(newDefinitions, newExport, environment);
    }

    public <T> T accept(ExpressionVisitor<T> visitor) throws PouletException {
        return visitor.visit(this);
    }

    public Fix context(Environment environment) throws PouletException {
        return new Fix(definitions, export, environment);
    }
}
