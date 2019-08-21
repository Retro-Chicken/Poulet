package poulet.superficial.ast;

import poulet.kernel.ast.TopLevel;
import poulet.parser.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Program extends Node {
    public final List<Node> nodes;

    public Program(List<Node> nodes) {
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
        for(Node node : nodes) {
            if(node instanceof TopLevel) {
                topLevels.add((TopLevel) node);
            } else if(node instanceof Sugar) {
                topLevels.addAll(((Sugar) node).inflate());
            }
        }
        return new poulet.kernel.ast.Program(topLevels);
    }
}
