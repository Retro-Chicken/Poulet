package poulet.parser;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import poulet.ast.*;
import java.util.ArrayList;
import java.util.List;

public class ASTParser extends PouletBaseListener {
    public static Program parse(CharStream stream) {
        PouletLexer lexer = new PouletLexer(stream);
        PouletParser parser = new PouletParser(new CommonTokenStream(lexer));
        return (Program) TreeReduce.reduce(parser.program(), ASTParser::reducer);
    }

    private static Node reducer(Object payload, List<Node> children) {
        if (payload instanceof PouletParser.ProgramContext) {
            List<TopLevel> topLevels = new ArrayList<>();

            for (Node child : children) {
                topLevels.add((TopLevel) child);
            }

            return new Program(topLevels);
        } else if (payload instanceof PouletParser.DefinitionContext) {
            Symbol name = (Symbol) children.get(0);
            Expression type = (Expression) children.get(2);

            switch (children.size()) {
                case 3:
                    return new Definition(name, type);
                case 5:
                    Expression definition = (Expression) children.get(4);
                    return new Definition(name, type, definition);
            }
        } else if (payload instanceof PouletParser.PrintContext) {
            PouletParser.PrintContext context = (PouletParser.PrintContext) payload;
            Print.PrintCommand printCommand;

            if (context.REDUCE() != null)
                printCommand = Print.PrintCommand.reduce;
            else if (context.CHECK() != null)
                printCommand = Print.PrintCommand.check;
            else if (context.SCHOLIUMS() != null)
                printCommand = Print.PrintCommand.scholiums;
            else
                return null;

            Expression expression = (Expression) children.get(1);
            return new Print(printCommand, expression);
        } else if (payload instanceof PouletParser.Type_declarationContext) {
            Symbol name = (Symbol) children.get(1);
            Expression type = (Expression) children.get(3);
            List<Constructor> constructors = new ArrayList<>();

            for (int i = 5; i < children.size() - 1; i++)
                constructors.add((Constructor) children.get(i));

            return new TypeDeclaration(name, type, constructors);
        } else if (payload instanceof PouletParser.ExpressionContext) {
            return children.get(0);
        } else if (payload instanceof PouletParser.VariableContext) {
            Symbol name;
            switch (children.size()) {
                case 1:
                    name = (Symbol) children.get(0);
                    return new Variable(name);
                case 3:
                    Symbol type = (Symbol) children.get(0);
                    name = (Symbol) children.get(2);
                    return new Variable(type, name);
            }
        } else if (payload instanceof PouletParser.AbstractionContext) {
            Symbol variable = (Symbol) children.get(1);
            Expression type = (Expression) children.get(3);
            Expression body = (Expression) children.get(5);
            return new Abstraction(variable, type, body);
        } else if (payload instanceof PouletParser.ApplicationContext) {
            Expression function = (Expression) children.get(1);
            Expression argument = (Expression) children.get(3);
            return new Application(function, argument);
        } else if (payload instanceof PouletParser.Pi_typeContext) {
            Symbol variable = (Symbol) children.get(1);
            Expression type = (Expression) children.get(3);
            Expression body = (Expression) children.get(5);
            return new PiType(variable, type, body);
        } else if (payload instanceof PouletParser.ConstructorContext) {
            Symbol name = (Symbol) children.get(0);
            Expression definition = (Expression) children.get(2);
            return new Constructor(name, definition);
        } else if (payload instanceof PouletParser.SymbolContext) {
            PouletParser.SymbolContext context = (PouletParser.SymbolContext) payload;
            String symbol = context.SYMBOL().getText();
            return new Symbol(symbol);
        } else if(payload instanceof PouletParser.OutputContext) {
            PouletParser.OutputContext context = (PouletParser.OutputContext) payload;
            String text = context.STRING().getText();
            return new Output(text.substring(1, text.length() - 1));
        } else if (payload instanceof PouletParser.Inductive_typeContext) {
            PouletParser.Inductive_typeContext context = (PouletParser.Inductive_typeContext) payload;
            int nargs = Integer.parseInt(context.INTEGER().getText());
            List<TypeDeclaration> typeDeclarations = new ArrayList<>();
            for (int i = 3; i < children.size() - 1; i++) {
                typeDeclarations.add((TypeDeclaration) children.get(i));
            }
            return new InductiveDeclaration(nargs, typeDeclarations);
        }

        return null;
    }
}
