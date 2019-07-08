package poulet.ast;

import poulet.contextexpressions.ContextConstructorCall;
import poulet.exceptions.PouletException;
import poulet.typing.Environment;
import poulet.util.ExpressionVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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
            CharLiteral head = (CharLiteral) arguments.get(0);
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

    @Override
    ConstructorCall transformSymbols(Function<Symbol, Symbol> transformer, Map<Symbol, Symbol> map) throws PouletException {
        List<Expression> newArguments = null;
        if (isConcrete()) {
            newArguments = new ArrayList<>();

            for (Expression argument : arguments) {
                newArguments.add(argument.transformSymbols(transformer, map));
            }
        }

        return new ConstructorCall(
                inductiveType.transformSymbols(transformer, map),
                constructor,
                newArguments
        );
    }

    public <T> T accept(ExpressionVisitor<T> visitor) throws PouletException {
        return visitor.visit(this);
    }

    public ContextConstructorCall contextExpression(Environment environment) {
        return new ContextConstructorCall(this, environment);
    }
}
