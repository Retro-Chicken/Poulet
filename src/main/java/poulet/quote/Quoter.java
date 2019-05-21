package poulet.quote;

import poulet.ast.*;
import poulet.value.*;

import java.util.List;
import java.util.stream.Collectors;

public class Quoter {
    public static Expression quote(Value value) {
        if (value instanceof VAbstraction) {
            VAbstraction abstraction = (VAbstraction) value;
            Symbol temp = new Symbol("temp").makeUnique();
            VNeutral free = new VNeutral(new NFree(temp));
            return new Abstraction(temp, quote(abstraction.type), quote(abstraction.call(free)));
        } else if (value instanceof VNeutral) {
            VNeutral neutral = (VNeutral) value;
            return neutralQuote(neutral.neutral);
        } else if (value instanceof VType) {
            VType type = (VType) value;
            return new Variable(new Symbol("Type" + type.level));
        } else if (value instanceof VPi) {
            VPi piType = (VPi) value;
            Symbol temp = new Symbol("temp").makeUnique();
            VNeutral free = new VNeutral(new NFree(temp));
            return new PiType(temp, quote(piType.type), quote(piType.call(free)));
        } else if (value instanceof VInductiveType) {
            VInductiveType inductiveType = (VInductiveType) value;
            List<Expression> parameters = inductiveType.parameters.stream().map(Quoter::quote).collect(Collectors.toList());
            List<Expression> arguments = null;

            if (inductiveType.concrete)
                arguments = inductiveType.arguments.stream().map(Quoter::quote).collect(Collectors.toList());

            return new InductiveType(
                    inductiveType.typeDeclaration.name,
                    inductiveType.concrete,
                    parameters,
                    arguments
            );
        } else if (value instanceof VConstructed) {
            VConstructed constructed = (VConstructed) value;
            List<Expression> arguments = constructed.arguments.stream().map(Quoter::quote).collect(Collectors.toList());
            InductiveType inductiveType = (InductiveType) quote(constructed.inductiveType);

            return new ConstructorCall(
                    inductiveType,
                    constructed.constructor.name,
                    arguments
            );
        } else if (value instanceof VFix) {
            VFix fix = (VFix) value;
            return fix.fix;
        } else if (value instanceof VChar) {
            VChar c = (VChar) value;
            return c.c;
        }

        return null;
    }

    private static Expression neutralQuote(Neutral neutral) {
        if (neutral instanceof NFree) {
            NFree free = (NFree) neutral;
            return new Variable(free.symbol);
        } else if (neutral instanceof NApplication) {
            NApplication application = (NApplication) neutral;
            return new Application(neutralQuote(application.function), quote(application.argument));
        }
        return null;
    }
}
