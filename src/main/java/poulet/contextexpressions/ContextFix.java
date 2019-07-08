package poulet.contextexpressions;

import poulet.ast.Definition;
import poulet.ast.Fix;
import poulet.ast.Symbol;
import poulet.exceptions.PouletException;
import poulet.typing.Environment;
import poulet.util.ContextExpressionVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ContextFix extends ContextExpression {
    public final List<ContextDefinition> definitions;
    public final Symbol export;

    public ContextFix(Fix fix, Environment environment) throws PouletException {
        super(fix, environment);
        this.export = fix.export;
        Environment innerEnvironment = environment;
        for(Definition definition : fix.definitions)
            innerEnvironment = innerEnvironment.appendType(definition.name, definition.type);
        List<ContextDefinition> definitions = new ArrayList<>();
        for(Definition definition : fix.definitions)
            definitions.add(new ContextDefinition(definition.name, definition.type.contextExpression(environment), definition.definition.contextExpression(innerEnvironment)));
        this.definitions = definitions;
    }

    public ContextDefinition getExported() throws PouletException {
        for (ContextDefinition definition : definitions) {
            if (definition.name.equals(export)) {
                return definition;
            }
        }

        throw new PouletException("symbol " + export + " not defined in " + this);
    }

    public <T> T accept(ContextExpressionVisitor<T> visitor) throws PouletException {
        return visitor.visit(this);
    }
}
