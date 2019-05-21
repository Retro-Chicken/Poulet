package poulet.ast;

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
        return arguments != null;
    }

    private boolean isCharList() {
        if (!isConcrete())
            return false;

        if (!inductiveType.type.equals(new Symbol("list")))
            return false;

        if (!(inductiveType.parameters.get(0) instanceof Variable))
            return false;

        Variable parameter = (Variable) inductiveType.parameters.get(0);

        return parameter.symbol.equals(new Symbol("char"));
    }

    private String charListToString() {
        if (constructor.equals(new Symbol("nil"))) {
            return "";
        } else {
            Char head = (Char) arguments.get(0);
            ConstructorCall tail = (ConstructorCall) arguments.get(1);
            return head.c + tail.charListToString();
        }
    }

    @Override
    public String toString() {
        if (isCharList()) {
            return "\"" + charListToString() + "\"";
        } else {
            String s = inductiveType.toString() + '.' + constructor;

            if (isConcrete()) {
                s += '[';
                s += arguments.stream().map(Expression::toString).collect(Collectors.joining(", "));
                s += ']';
            }

            return s;
        }
    }
}
