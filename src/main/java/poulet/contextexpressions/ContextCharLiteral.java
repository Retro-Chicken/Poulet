package poulet.contextexpressions;

import poulet.ast.CharLiteral;
import poulet.exceptions.PouletException;
import poulet.typing.Environment;
import poulet.util.ContextExpressionVisitor;

public class ContextCharLiteral extends ContextExpression {
    public final char c;

    public ContextCharLiteral(CharLiteral charLiteral, Environment environment) throws PouletException {
        super(charLiteral, environment);
        this.c = charLiteral.c;
    }

    public <T> T accept(ContextExpressionVisitor<T> visitor) throws PouletException {
        return visitor.visit(this);
    }
}
