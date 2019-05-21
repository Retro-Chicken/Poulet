package poulet.parser;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import poulet.ast.*;

import java.util.ArrayList;
import java.util.Arrays;
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
                if (child == null) // sketchy but easiest way to ignore EOF
                    continue;

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
            List<Parameter> parameters = new ArrayList<>();
            List<Constructor> constructors = new ArrayList<>();

            int i = 2;
            for (; children.get(i) instanceof Parameter; i++)
                parameters.add((Parameter) children.get(i));

            i++;

            Expression type = (Expression) children.get(i);

            i += 2;

            for (; children.get(i) instanceof Constructor; i++)
                constructors.add((Constructor) children.get(i));

            return new TypeDeclaration(name, parameters, type, constructors);
        } else if (payload instanceof PouletParser.ExpressionContext) {
            PouletParser.ExpressionContext context = (PouletParser.ExpressionContext) payload;
            if (children.size() > 1) {
                if (context.children.size() > 2 && context.children.get(1).getText().equals("->")) {
                    Expression type = (Expression) children.get(0);
                    Expression body = (Expression) children.get(2);
                    return new PiType(
                            new Symbol("_"),
                            type,
                            body
                    );
                } else if (children.get(0) instanceof Expression) {
                    Expression function = (Expression) children.get(0);
                    List<Expression> arguments = new ArrayList<>();

                    for (int i = 2; i < children.size() - 1; i += 2) {
                        arguments.add((Expression) children.get(i));
                    }

                    Application application = new Application(
                            function,
                            arguments.get(0)
                    );

                    for (int i = 1; i < arguments.size(); i++) {
                        application = new Application(
                                application,
                                arguments.get(i)
                        );
                    }

                    return application;
                } else {
                    return children.get(1);
                }
            } else {
                return children.get(0);
            }
        } else if (payload instanceof PouletParser.VariableContext) {
            Symbol name = (Symbol) children.get(0);
            return new Variable(name);
        } else if (payload instanceof PouletParser.AbstractionContext) {
            Symbol variable = (Symbol) children.get(1);
            Expression type = (Expression) children.get(3);
            Expression body = (Expression) children.get(5);
            return new Abstraction(variable, type, body);
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
        } else if (payload instanceof PouletParser.Assert_eqContext) {
            Expression a = (Expression) children.get(1);
            Expression b = (Expression) children.get(3);
            return new Assert(a, b);
        } else if (payload instanceof PouletParser.Inductive_typesContext) {
            List<TypeDeclaration> typeDeclarations = new ArrayList<>();
            for (int i = 2; i < children.size() - 1; i++) {
                typeDeclarations.add((TypeDeclaration) children.get(i));
            }
            return new InductiveDeclaration(typeDeclarations);
        } else if (payload instanceof PouletParser.ParameterContext) {
            Symbol symbol = (Symbol) children.get(1);
            Expression type = (Expression) children.get(3);
            return new Parameter(symbol, type);
        } else if (payload instanceof PouletParser.Inductive_typeContext) {
            Symbol type = (Symbol) children.get(0);
            List<Expression> parameters = new ArrayList<>();
            for (int i = 1; i + 1 < children.size() && children.get(i + 1) instanceof Expression; i += 2) {
                parameters.add((Expression) children.get(i + 1));
            }
            return new InductiveType(type, false, parameters);
        } else if (payload instanceof PouletParser.Constructor_callContext) {
            InductiveType inductiveType = (InductiveType) children.get(0);
            Symbol constructor = (Symbol) children.get(2);
            return new ConstructorCall(inductiveType, constructor);
        } else if (payload instanceof PouletParser.MatchContext) {
            Expression expression = (Expression) children.get(1);
            Symbol expressionSymbol = (Symbol) children.get(3);
            List<Symbol> argumentSymbols = new ArrayList<>();

            int i = 5;

            for (; children.get(i) instanceof Symbol; i++) {
                argumentSymbols.add((Symbol) children.get(i));
            }

            i += 2;
            Expression type = (Expression) children.get(i);
            i += 2;

            List<Match.Clause> clauses = new ArrayList<>();

            for (; i < children.size() - 1 && children.get(i) instanceof Match.Clause; i += 2) {
                clauses.add((Match.Clause) children.get(i));
            }

            return new Match(expression, expressionSymbol, argumentSymbols, type, clauses);
        } else if (payload instanceof PouletParser.Match_clauseContext) {
            Symbol expressionSymbol = (Symbol) children.get(0);
            List<Symbol> argumentSymbols = new ArrayList<>();

            for (int i = 1; i < children.size() - 3 && children.get(i + 1) instanceof Symbol; i += 2) {
                argumentSymbols.add((Symbol) children.get(i + 1));
            }

            Expression expression = (Expression) children.get(children.size() - 1);

            return new Match.Clause(expressionSymbol, argumentSymbols, expression);
        } else if (payload instanceof PouletParser.Import_commandContext) {
            PouletParser.Import_commandContext context = (PouletParser.Import_commandContext) payload;
            String text = context.STRING().getText();
            return new Import(text.substring(1, text.length() - 1));
        } else if (payload instanceof PouletParser.Fix_definitionContext) {
            Symbol name = (Symbol) children.get(0);
            Expression type = (Expression) children.get(2);
            Expression definition = (Expression) children.get(4);
            return new Definition(name, type, definition);
        } else if (payload instanceof PouletParser.FixContext) {
            List<Definition> definitions = new ArrayList<>();
            for (int i = 2; i < children.size() - 3; i++) {
                definitions.add((Definition) children.get(i));
            }
            Symbol symbol = (Symbol) children.get(children.size() - 1);
            return new Fix(definitions, symbol);
        } else if (payload instanceof PouletParser.Toplevel_fixContext) {
            Definition definition = (Definition) children.get(1);

            if (definition.definition == null) {
                System.err.println("top level fix can't have null definition");
                return null;
            }

            Fix fix = new Fix(
                    Arrays.asList(definition),
                    definition.name
            );
            return new Definition(
                    definition.name,
                    definition.type,
                    fix
            );
        } else if (payload instanceof PouletParser.CharacterContext) {
            PouletParser.CharacterContext context = (PouletParser.CharacterContext) payload;
            char c = context.CHAR().getText().charAt(1);
            return new Char(c);
        } else if (payload instanceof PouletParser.StringContext) {
            PouletParser.StringContext context = (PouletParser.StringContext) payload;
            String withQuotes = context.STRING().getText();
            String s = withQuotes.substring(1, withQuotes.length() - 1);
            InductiveType listChar = new InductiveType(
                    new Symbol("list"),
                    false,
                    Arrays.asList(
                            new Variable(new Symbol("char"))
                    )
            );

            Expression result = new ConstructorCall(
                    listChar,
                    new Symbol("nil"),
                    Arrays.asList()
            );

            for (int i = s.length() - 1; i >= 0; i--) {
                result = new ConstructorCall(
                        listChar,
                        new Symbol("cons"),
                        Arrays.asList(
                                new Char(s.charAt(i)),
                                result
                        )
                );
            }

            return result;
        }

        return null;
    }
}
