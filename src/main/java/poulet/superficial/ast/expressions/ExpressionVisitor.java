package poulet.superficial.ast.expressions;

import poulet.PouletException;
import poulet.superficial.ast.inlines.LetIn;
import poulet.superficial.ast.inlines.Where;

public interface ExpressionVisitor<T> {
    default T visit(Abstraction abstraction) {
        return other(abstraction);
    }

    default T visit(Application application) {
        return other(application);
    }

    default T visit(ConstructorCall constructorCall) {
        return other(constructorCall);
    }

    default T visit(Fix fix) {
        return other(fix);
    }

    default T visit(InductiveType inductiveType) {
        return other(inductiveType);
    }

    default T visit(Match match) {
        return other(match);
    }

    default T visit(MetaVar metaVar) {
        return other(metaVar);
    }

    default T visit(Prod prod) {
        return other(prod);
    }

    default T visit(Prop prop) {
        return other(prop);
    }

    default T visit(Set set) {
        return other(set);
    }

    default T visit(Type type) {
        return other(type);
    }

    default T visit(Var var) {
        return other(var);
    }

    default T visit(LetIn letIn) {
        return other(letIn);
    }

    default T visit(Where where) {
        return other(where);
    }

    default T other(Expression expression) {
        throw new PouletException("ExpressionVisitor doesn't cover " + expression.getClass().getSimpleName());
    }
}
