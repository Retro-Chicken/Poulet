package poulet.refiner.ast;

import poulet.parser.Node;

import java.util.List;
import java.util.stream.Collectors;

public class Program extends Node {
    public final List<Node> nodes;

    public Program(List<Node> nodes) {
        this.nodes = nodes;
    }

    @Override
    public String toString() {
        return nodes.stream()
                .map(Node::toString)
                .collect(Collectors.joining("\n"));
    }
}
