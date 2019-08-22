package poulet.superficial;

import poulet.parser.KernelNode;
import poulet.superficial.ast.inlines.Inline;
import poulet.superficial.ast.multilines.Multiline;
import poulet.parser.SuperficialNode;
import poulet.superficial.ast.expressions.Expression;

import java.util.ArrayList;
import java.util.List;

public class Desugar {
    public static KernelNode desugar(Inline inlineSugar) {
        if(inlineSugar instanceof Inline.Projectable) {
            return ((Inline.Projectable) inlineSugar).project();
        } else if(inlineSugar instanceof Inline.Transformable) {
            return desugar(((Inline.Transformable) inlineSugar).transform());
        }
        return null;
    }

    public static List<KernelNode> desugar(Multiline multilineSugar) {
        List<KernelNode> result = new ArrayList<>();
        for(SuperficialNode sugar : multilineSugar.inflate()) {
            if(sugar instanceof Inline) {
                result.add(desugar((Inline) sugar));
            } else if(sugar instanceof Multiline) {
                result.addAll(desugar((Multiline) sugar));
            }
        }
        return result;
    }

    public static poulet.kernel.ast.Expression desugar(Expression expression) {
        if(expression instanceof Expression.Projectable)
            return ((Expression.Projectable) expression).project();
        else if(expression instanceof Expression.Transformable)
            return desugar(((Expression.Transformable) expression).transform());
        return null;
    }
}
