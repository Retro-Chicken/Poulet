package poulet.ast;

import poulet.contextexpressions.ContextFix;
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

    public Fix(List<Definition> definitions, Symbol export) {
        this.definitions = definitions;
        this.export = export;
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

        return new Fix(newDefinitions, newExport);
    }

    public <T> T accept(ExpressionVisitor<T> visitor) throws PouletException {
        return visitor.visit(this);
    }

    public ContextFix contextExpression(Environment environment) throws PouletException {
        return new ContextFix(this, environment);
    }
}
