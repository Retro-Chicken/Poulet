package poulet.contextexpressions;

import poulet.ast.Definition;
import poulet.ast.Fix;
import poulet.ast.Symbol;
import poulet.exceptions.PouletException;
import poulet.typing.Environment;

import java.util.List;
import java.util.stream.Collectors;

public class ContextFix extends ContextExpression {
    public final List<ContextDefinition> definitions;
    public final Symbol export;

    public ContextFix(Fix fix, Environment environment) {
        super(fix, environment);
        this.export = fix.export;
        Environment innerEnvironment = environment;
        for(Definition definition : fix.definitions)
            innerEnvironment = innerEnvironment.appendType(definition.name, definition.type);
        final Environment finalInnerEnvironment = innerEnvironment;
        this.definitions = fix.definitions.stream().map(x -> new ContextDefinition(x.name,
                x.type.contextExpression(environment), x.definition.contextExpression(finalInnerEnvironment))).collect(Collectors.toList());
    }

    public ContextDefinition getExported() throws PouletException {
        for (ContextDefinition definition : definitions) {
            if (definition.name.equals(export)) {
                return definition;
            }
        }

        throw new PouletException("symbol " + export + " not defined in " + this);
    }
}
