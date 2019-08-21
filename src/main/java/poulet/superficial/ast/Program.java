package poulet.superficial.ast;

import poulet.kernel.ast.TopLevel;
import poulet.parser.KernelAST;
import poulet.parser.Node;
import poulet.superficial.Desugar;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Program extends Sugar {
    public final List<Sugar> nodes;

    public Program(List<Sugar> nodes) {
        this.nodes = nodes;
    }

    public Section getSection(String name) {
        for(Node node : nodes) {
            if(node instanceof Section) {
                if(((Section) node).name.equals(name))
                    return (Section) node;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return nodes.stream()
                .map(Node::toString)
                .collect(Collectors.joining("\n"));
    }

    public poulet.kernel.ast.Program project() {
        List<TopLevel> topLevels = new ArrayList<>();
        for(Sugar node : nodes) {
            if(node instanceof Multiline) {
                for(KernelAST line : Desugar.desugar((Multiline) node))
                    topLevels.add((TopLevel) line);
            } else if(node instanceof Inline) {
                topLevels.add((TopLevel) Desugar.desugar((Inline) node));
            }
        }
        return new poulet.kernel.ast.Program(topLevels);
    }
}
