package poulet.superficial;

import poulet.kernel.ast.TopLevel;
import poulet.parser.KernelAST;
import poulet.superficial.ast.Inline;
import poulet.superficial.ast.Multiline;
import poulet.superficial.ast.Sugar;

import java.util.ArrayList;
import java.util.List;

public class Desugar {
    public static KernelAST desugar(Inline inlineSugar) {
        if(inlineSugar instanceof Inline.Projectable) {
            return ((Inline.Projectable) inlineSugar).project();
        } else if(inlineSugar instanceof Inline.Transformable) {
            return desugar(((Inline.Transformable) inlineSugar).transform());
        }
        return null;
    }

    public static List<KernelAST> desugar(Multiline multilineSugar) {
        List<KernelAST> result = new ArrayList<>();
        for(Sugar sugar : multilineSugar.inflate()) {
            if(sugar instanceof Inline) {
                result.add(desugar((Inline) sugar));
            } else if(sugar instanceof Multiline) {
                result.addAll(desugar((Multiline) sugar));
            }
        }
        return result;
    }
}
