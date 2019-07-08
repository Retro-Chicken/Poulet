package poulet.contextexpressions;

import poulet.ast.Definition;
import poulet.ast.Symbol;
import poulet.exceptions.PouletException;
import poulet.typing.Environment;

public class ContextDefinition extends ContextTopLevel {
    public final Symbol name;
    public final ContextExpression type;
    public final ContextExpression definition;

    public ContextDefinition(Definition definition, Environment environment) throws PouletException {
        super(definition, environment);
        this.name = definition.name;
        this.type = definition.type.contextExpression(environment);
        this.definition = definition.definition.contextExpression(environment);
    }

    public ContextDefinition(Symbol name, ContextExpression type, ContextExpression definition) {
        super(new Definition(name, type.expression, definition.expression), type.environment);
        this.name = name;
        this.type = type;
        this.definition = definition;
    }
}
