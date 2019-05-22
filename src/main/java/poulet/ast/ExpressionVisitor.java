package poulet.ast;

import poulet.exceptions.PouletException;

public interface ExpressionVisitor<T> {
    default T visit(Abstraction abstraction) throws PouletException {
        return other(abstraction);
    }

    default T visit(Application application) throws PouletException {
        return other(application);
    }

    default T visit(CharLiteral charLiteral) throws PouletException {
        return other(charLiteral);
    }

    default T visit(ConstructorCall constructorCall) throws PouletException {
        return other(constructorCall);
    }

    default T visit(Fix fix) throws PouletException {
        return other(fix);
    }

    default T visit(InductiveType inductiveType) throws PouletException {
        return other(inductiveType);
    }

    default T visit(Match match) throws PouletException {
        return other(match);
    }

    default T visit(PiType piType) throws PouletException {
        return other(piType);
    }

    default T visit(Variable variable) throws PouletException {
        return other(variable);
    }

    default T other(Expression expression) throws PouletException {
        throw new PouletException(expression.getClass().getSimpleName() + " not handled by visitor");
    }
}
