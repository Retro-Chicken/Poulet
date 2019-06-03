package poulet.ast;

import poulet.util.StringUtil;
import poulet.exceptions.PouletException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TypeDeclaration extends Node {
    public final Symbol name;
    public final List<Parameter> parameters;
    public final Expression type;
    public final List<Constructor> constructors;
    private final InductiveDeclaration inductiveDeclaration;

    public TypeDeclaration(Symbol name, List<Parameter> parameters, Expression type, List<Constructor> constructors) {
        this.name = name;
        this.parameters = parameters;
        this.type = type;
        this.constructors = constructors;
        this.inductiveDeclaration = null;
    }

    public TypeDeclaration(Symbol name, List<Parameter> parameters, Expression type, List<Constructor> constructors, InductiveDeclaration inductiveDeclaration) {
        this.name = name;
        this.parameters = parameters;
        this.type = type;
        this.constructors = constructors;
        this.inductiveDeclaration = inductiveDeclaration;
    }

    public TypeDeclaration addInductiveDeclaration(InductiveDeclaration inductiveDeclaration) {
        return new TypeDeclaration(name, parameters, type, constructors, inductiveDeclaration);
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

    TypeDeclaration makeSymbolsUnique() throws PouletException {
        List<Parameter> newParameters = new ArrayList<>();
        Map<Symbol, Symbol> map = new HashMap<>();

        for (Parameter parameter : parameters) {
            Symbol unique = parameter.symbol.makeUnique();
            Parameter newParameter = new Parameter(unique, parameter.type);
            newParameters.add(newParameter);
            map.put(parameter.symbol, unique);
        }

        List<Constructor> newConstructors = new ArrayList<>();

        for (Constructor constructor : constructors) {
            Constructor unique = new Constructor(constructor.name, constructor.definition.makeSymbolsUnique(map));
            newConstructors.add(unique);
        }

        return new TypeDeclaration(
                name,
                newParameters,
                type.makeSymbolsUnique(map),
                newConstructors
        );
    }
}
