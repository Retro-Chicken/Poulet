package poulet.kernel.ast;

import poulet.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TypeDeclaration extends Node {
    public InductiveDeclaration inductiveDeclaration;
    public final Symbol name;
    public final List<Parameter> parameters;
    public final Expression type;
    public final List<Constructor> constructors;

    public static class Parameter extends Node {
        public final Symbol name;
        public final Expression type;

        public Parameter(Symbol name, Expression type) {
            this.name = name;
            this.type = type;
        }

        @Override
        public String toString() {
            return "(" + name + " : " + type + ")";
        }
    }

    public static class Constructor extends Node {
        public final Symbol name;
        public final Expression definition;

        public Constructor(Symbol name, Expression definition) {
            this.name = name;
            this.definition = definition;
        }

        @Override
        public String toString() {
            return String.format("%s : %s", name, definition);
        }
    }

    public TypeDeclaration(InductiveDeclaration inductiveDeclaration, Symbol name, List<Parameter> parameters, Expression type, List<Constructor> constructors) {
        this.inductiveDeclaration = inductiveDeclaration;
        this.name = name;
        this.parameters = parameters;
        this.type = type;
        this.constructors = constructors;
    }

    @Override
    public String toString() {
        String items = constructors.stream().map(Constructor::toString).collect(Collectors.joining("\n"));
        String s = "type " + name + (parameters.size() > 0 ? " " : "");
        s += parameters.stream().map(Parameter::toString).collect(Collectors.joining(" "));
        s += " : " + type + " {\n";
        s += StringUtil.indent(items, 2);
        s += "\n}";
        return s;
    }

    public TypeDeclaration makeSymbolsUnique() {
        List<Parameter> newParameters = new ArrayList<>();
        Map<Symbol, Symbol> map = new HashMap<>();

        for (Parameter parameter : parameters) {
            Symbol unique = new UniqueSymbol(parameter.name);
            map.put(parameter.name, unique);
            Parameter newParameter = new Parameter(unique, parameter.type.makeSymbolsUnique(map));
            newParameters.add(newParameter);
        }

        List<Constructor> newConstructors = new ArrayList<>();

        for (Constructor constructor : constructors) {
            Constructor unique = new Constructor(constructor.name, constructor.definition.makeSymbolsUnique(map));
            newConstructors.add(unique);
        }

        return new TypeDeclaration(
                inductiveDeclaration,
                name,
                newParameters,
                type.makeSymbolsUnique(map),
                newConstructors
        );
    }
}
