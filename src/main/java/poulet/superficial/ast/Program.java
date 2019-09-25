package poulet.superficial.ast;

import poulet.kernel.ast.TopLevel;
import poulet.parser.KernelNode;
import poulet.parser.Node;
import poulet.parser.SuperficialNode;
import poulet.superficial.Desugar;
import poulet.superficial.ast.inlines.Inline;
import poulet.superficial.ast.multilines.Multiline;
import poulet.superficial.ast.multilines.Section;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Program extends SuperficialNode {
    public final List<SuperficialNode> nodes;

    public Program(List<SuperficialNode> nodes) {
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
        for(SuperficialNode node : nodes) {
            if(node instanceof Multiline) {
                for(KernelNode line : Desugar.desugar((Multiline) node))
                    topLevels.add((TopLevel) line);
            } else if(node instanceof Inline) {
                topLevels.add((TopLevel) Desugar.desugar((Inline) node));
            }
        }
        return new poulet.kernel.ast.Program(topLevels);
    }

    public Program inflate() {
        List<SuperficialNode> result = new ArrayList<>(nodes);
        for(int i = 0; i < result.size(); i++) {
            if(result.get(i) instanceof Multiline) {
                result.addAll(i, ((Multiline) result.remove(i)).inflate());
                i--;
            }
        }
        return new Program(result);
    }

    @Override
    public Program makeSymbolsUnique() {
        return new Program(nodes.stream().map(x -> x.makeSymbolsUnique()).collect(Collectors.toList()));
    }
}
