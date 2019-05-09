package poulet.value;

import poulet.ast.Expression;
import poulet.quote.Quoter;

public abstract class Value {
    @Override
    public String toString() {
        return expression().toString();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Value) {
            Value value = (Value) obj;
            return expression().equals(value.expression());
        }
        return false;
    }

    public Expression expression() {
        return Quoter.quote(this);
    }
}
