package poulet.contextexpressions;

import poulet.ast.Definition;
import poulet.ast.Expression;
import poulet.ast.Symbol;
import poulet.typing.Environment;

public class ContextDefinition {
    public final Symbol name;
    public final ContextExpression type;
    public final ContextExpression definition;

    public ContextDefinition(Definition definition, Environment environment) {
        this.name = definition.name;
        this.type = definition.type.contextExpression(environment);
        this.definition = definition.definition.contextExpression(environment);
    }

    public ContextDefinition(Symbol name, ContextExpression type, ContextExpression definition) {
        this.name = name;
        this.type = type;
        this.definition = definition;
    }
}
