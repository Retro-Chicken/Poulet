package poulet.parser;

import org.antlr.v4.runtime.tree.Tree;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

class TreeReduce {
    static <T> T reduce(Tree tree, BiFunction<Object, List<T>, T> reducer) {
        Object payload = tree.getPayload();
        List<T> reducedChildren = new ArrayList<>();

        for (int i = 0; i < tree.getChildCount(); i++) {
            Tree child = tree.getChild(i);
            T reducedChild = reduce(child, reducer);
            reducedChildren.add(reducedChild);
        }

        return reducer.apply(payload, reducedChildren);
    }
}
