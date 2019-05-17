package poulet.ast;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ConstructorCall extends Expression {
    public final InductiveType inductiveType;
    public final Symbol constructor;
    public final List<Expression> arguments;

    public ConstructorCall(InductiveType inductiveType, Symbol constructor) {
        this(inductiveType, constructor, null);
    }

    public ConstructorCall(InductiveType inductiveType, Symbol constructor, List<Expression> arguments) {
        this.inductiveType = inductiveType;
        this.constructor = constructor;
        this.arguments = arguments;
    }

    public boolean isConcrete() {
        return this.arguments != null;
    }

    @Override
    public String toString() {
        String s = inductiveType.toString() + '.' + constructor;

        if (isConcrete()) {
            s += '(';
            s += arguments.stream().map(Expression::toString).collect(Collectors.joining(", "));
            s += ')';
        }

        return s;
    }
}
