package poulet.util;

import poulet.contextexpressions.*;
import poulet.exceptions.PouletException;

public interface ContextExpressionVisitor<T> {
    default T visit(ContextAbstraction abstraction) throws PouletException {
        return other(abstraction);
    }

    default T visit(ContextApplication application) throws PouletException {
        return other(application);
    }

    default T visit(ContextCharLiteral charLiteral) throws PouletException {
        return other(charLiteral);
    }

    default T visit(ContextConstructorCall constructorCall) throws PouletException {
        return other(constructorCall);
    }

    default T visit(ContextFix fix) throws PouletException {
        return other(fix);
    }

    default T visit(ContextInductiveType inductiveType) throws PouletException {
        return other(inductiveType);
    }

    default T visit(ContextMatch match) throws PouletException {
        return other(match);
    }

    default T visit(ContextPiType piType) throws PouletException {
        return other(piType);
    }

    default T visit(ContextProp prop) throws PouletException {
        return other(prop);
    }

    default T visit(ContextSet set) throws PouletException {
        return other(set);
    }

    default T visit(ContextType type) throws PouletException {
        return other(type);
    }

    default T visit(ContextVariable variable) throws PouletException {
        return other(variable);
    }

    default T other(ContextExpression expression) throws PouletException {
        throw new PouletException(expression.getClass().getSimpleName() + " not handled by visitor, expression = " + expression);
    }
}
