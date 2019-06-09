package poulet.util;

import poulet.ast.*;
import poulet.exceptions.PouletException;

public interface TopLevelVisitor<T> {
    default T visit(Assertion assertion) throws PouletException {
        return other(assertion);
    }

    default T visit(Definition definition) throws PouletException {
        return other(definition);
    }

    default T visit(Import importStatement) throws PouletException {
        return other(importStatement);
    }

    default T visit(InductiveDeclaration inductiveDeclaration) throws PouletException {
        return other(inductiveDeclaration);
    }

    default T visit(Print print) throws PouletException {
        return other(print);
    }

    default T other(TopLevel topLevel) throws PouletException {
        throw new PouletException(topLevel.getClass().getSimpleName() + " not handled by visitor");
    }
}
