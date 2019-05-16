package poulet.quote;

import poulet.ast.*;
import poulet.value.*;

public class Quoter {

    public static Expression quote(Value value) {
        return quote(value, 0);
    }

    private static Value vFree(int i) {
        return new VNeutral(new NFree(new Quote(i)));
    }

    private static Expression quote(Value value, int i) {
        if(value instanceof VAbstraction) {
            VAbstraction abstraction = (VAbstraction) value;
            return new Abstraction(null, quote(abstraction.type, i), quote(abstraction.call(vFree(i)), i + 1));
        } else if(value instanceof VNeutral) {
            VNeutral neutral = (VNeutral) value;
            return neutralQuote(neutral.neutral, i);
        } else if(value instanceof VType) {
            VType type = (VType) value;
            return new Variable(new Symbol("Type" + type.level));
        } else if(value instanceof VPi) {
            VPi piType = (VPi) value;
            return new PiType(null, quote(piType.type, i), quote(piType.call(vFree(i)), i + 1));
        }
        return null;
    }

    private static Expression neutralQuote(Neutral neutral, int i) {
        if(neutral instanceof NFree) {
            NFree free = (NFree) neutral;
            if(free.symbol instanceof Quote) {
                Quote quote = (Quote) free.symbol;
                int index = i - quote.level - 1;
                return new Variable(new Symbol(index));
            } else {
                return new Variable(free.symbol);
            }
        } else if(neutral instanceof NApplication) {
            NApplication application = (NApplication) neutral;
            return new Application(neutralQuote(application.function, i), quote(application.argument, i));
        }
        return null;
    }
}
