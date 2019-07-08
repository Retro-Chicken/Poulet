package poulet.contextexpressions;

import poulet.ast.TopLevel;
import poulet.typing.Environment;

public abstract class ContextTopLevel {
    public final TopLevel topLevel;
    public final Environment environment;

    protected ContextTopLevel(TopLevel topLevel, Environment environment) {
        this.topLevel = topLevel;
        this.environment = environment;
    }

    @Override
    public String toString() {
        return topLevel.toString() + "\nIn Environment\n" + environment.toString();
    }

    @Override
    public boolean equals(Object object) {
        if(object instanceof ContextTopLevel) {
            ContextTopLevel other = (ContextTopLevel) object;
            return topLevel.equals(other.topLevel) && environment.equals(other.environment);
        }
        return false;
    }
}
