package poulet.superficial.ast.multilines;

import poulet.parser.SuperficialNode;
import poulet.superficial.Substituter;
import poulet.superficial.ast.expressions.*;
import poulet.superficial.ast.inlines.LetIn;
import poulet.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Proof extends Multiline {
    public Symbol name;
    public Expression type;
    public Expression proof;
    public List<Definition> lemmas;

    public Proof(Symbol name, Expression type, Expression proof, List<Definition> lemmas) {
        this.name = name;
        this.type = type;
        this.proof = proof;
        this.lemmas = lemmas;
    }

    @Override
    public List<SuperficialNode> inflate() {
        List<SuperficialNode> result = new ArrayList<>();
        Expression proof = this.proof;
        for(Definition lemma : lemmas)
            proof = new LetIn(lemma.name, lemma.definition, proof);
        List<UniqueSymbol> newNames = lemmas.stream().map(x -> x.name)
                                        .map(UniqueSymbol::new).collect(Collectors.toList());
        List<Definition> newLemmas = new ArrayList<>();
        for(int i = 0; i < lemmas.size(); i++) {
            Expression newDefinition = lemmas.get(i).definition;
            Expression newType = lemmas.get(i).type;
            for(int j = 0; j < lemmas.size(); j++) {
                newDefinition = Substituter.substitute(newDefinition, lemmas.get(j).name, new Var(newNames.get(j)));
                newType = Substituter.substitute(newType, lemmas.get(j).name, new Var(newNames.get(j)));
            }
            newLemmas.add(new Definition(newNames.get(i), newType, newDefinition));
        }
        for(Definition newLemma : newLemmas)
            result.add(newLemma);
        result.add(new Definition(name, type, proof));
        return result;
    }

    @Override
    public Proof makeSymbolsUnique() {
        return new Proof(
                name,
                type.makeSymbolsUnique(),
                proof.makeSymbolsUnique(),
                lemmas.stream().map(x -> x.makeSymbolsUnique()).collect(Collectors.toList())
        );
    }

    @Override
    public String toString() {
        return "" + name + " : " + type
                + " := Proof {" + "\n"
                + StringUtil.indent(lemmas.stream().map(Definition::toString).collect(Collectors.joining("\n"))
                + "\n" + proof, 2) + "\n"
                + "}";
    }
}
