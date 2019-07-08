package poulet.contextexpressions;

import poulet.ast.CharLiteral;
import poulet.typing.Environment;

public class ContextCharLiteral extends ContextExpression {
    public final char c;

    public ContextCharLiteral(CharLiteral charLiteral, Environment environment) {
        super(charLiteral, environment);
        this.c = charLiteral.c;
    }
}
