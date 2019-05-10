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
        // TODO: Figure out why checking strings works but checking Expression equality doesn't
        if(obj instanceof Value) {
            Value value = (Value) obj;
            return expression().toString().equals(value.expression().toString());
        }
        return false;
    }

    public Expression expression() {
        return Quoter.quote(this);
    }
}
