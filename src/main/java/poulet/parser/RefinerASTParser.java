package poulet.parser;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import poulet.kernel.ast.*;
import poulet.refiner.ast.Program;

import poulet.refiner.ast.*;
import poulet.parser.refiner.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RefinerASTParser {
    public static Program parse(CharStream stream) {
        RefinerLexer lexer = new RefinerLexer(stream);
        RefinerParser parser = new RefinerParser(new CommonTokenStream(lexer));
        return (Program) TreeReduce.reduce(parser.program(), RefinerASTParser::reducer);
    }

    private static Node reducer(Object payload, List<Node> children) {
        if (payload instanceof RefinerParser.ProgramContext) {
            List<Node> nodes = new ArrayList<>();

            for (Node child : children) {
                if (child == null) // sketchy but easiest way to ignore EOF
                    continue;

                if (child instanceof TypeDeclaration) {
                    InductiveDeclaration inductiveDeclaration = new InductiveDeclaration(
                            Arrays.asList((TypeDeclaration) child)
                    );
                    nodes.add(inductiveDeclaration);
                } else {
                    nodes.add(child);
                }
            }

            return new Program(nodes);
        } else if (payload instanceof RefinerParser.DefinitionContext) {
            Symbol name = (Symbol) children.get(0);
            Expression type = (Expression) children.get(2);

            switch (children.size()) {
                case 3:
                    return new Definition(name, type);
                case 5:
                    Expression definition = (Expression) children.get(4);
                    return new Definition(name, type, definition);
            }
        } else if (payload instanceof RefinerParser.CommandContext) {
            RefinerParser.CommandContext context = (RefinerParser.CommandContext) payload;

            if (context.REDUCE() != null) {
                return new Command(
                        Command.Action.REDUCE,
                        List.of((Expression) children.get(1))
                );
            } else if (context.DEDUCE() != null) {
                return new Command(
                        Command.Action.DEDUCE,
                        List.of((Expression) children.get(1))
                );
            } else if (context.ASSERT() != null) {
                return new Command(
                        Command.Action.ASSERT,
                        List.of((Expression) children.get(1), (Expression) children.get(3))
                );
            }

            return null;
        } else if (payload instanceof RefinerParser.Type_declarationContext) {
            Symbol name = (Symbol) children.get(1);
            List<TypeDeclaration.Parameter> parameters = new ArrayList<>();
            List<TypeDeclaration.Constructor> constructors = new ArrayList<>();

            int i = 2;
            for (; children.get(i) instanceof TypeDeclaration.Parameter; i++)
                parameters.add((TypeDeclaration.Parameter) children.get(i));

            i++;

            Expression type = (Expression) children.get(i);

            i += 2;

            for (; children.get(i) instanceof TypeDeclaration.Constructor; i++)
                constructors.add((TypeDeclaration.Constructor) children.get(i));

            TypeDeclaration typeDeclaration = new TypeDeclaration(null, name, parameters, type, constructors);
            return typeDeclaration;
        } else if (payload instanceof RefinerParser.ExpressionContext) {
            RefinerParser.ExpressionContext context = (RefinerParser.ExpressionContext) payload;
            if (children.size() > 1) {
                if (context.children.size() > 2 && context.children.get(1).getText().equals("->")) {
                    Expression type = (Expression) children.get(0);
                    Expression body = (Expression) children.get(2);
                    return new Prod(
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
        } else if (payload instanceof RefinerParser.VariableContext) {
            Symbol name = (Symbol) children.get(0);
            return new Var(name);
        } else if (payload instanceof RefinerParser.AbstractionContext) {
            RefinerParser.AbstractionContext context = (RefinerParser.AbstractionContext) payload;
            Symbol variable = (Symbol) children.get(1);
            Expression type = (Expression) children.get(3);
            Expression body = (Expression) children.get(5);
            return new Abstraction(variable, type, body);
        } else if (payload instanceof RefinerParser.Pi_typeContext) {
            RefinerParser.Pi_typeContext context = (RefinerParser.Pi_typeContext) payload;
            Symbol variable = (Symbol) children.get(1);
            Expression type = (Expression) children.get(3);
            Expression body = (Expression) children.get(5);
            return new Prod(variable, type, body);
        } else if (payload instanceof RefinerParser.ConstructorContext) {
            Symbol name = (Symbol) children.get(0);
            Expression definition = (Expression) children.get(2);
            return new TypeDeclaration.Constructor(name, definition);
        } else if (payload instanceof RefinerParser.SymbolContext) {
            RefinerParser.SymbolContext context = (RefinerParser.SymbolContext) payload;
            String symbol = context.SYMBOL().getText();
            return new Symbol(symbol);
        } else if (payload instanceof RefinerParser.Inductive_typesContext) {
            List<TypeDeclaration> typeDeclarations = new ArrayList<>();
            for (int i = 2; i < children.size() - 1; i++) {
                typeDeclarations.add((TypeDeclaration) children.get(i));
            }

            InductiveDeclaration inductiveDeclaration = new InductiveDeclaration(typeDeclarations);
            for (TypeDeclaration typeDeclaration : inductiveDeclaration.typeDeclarations) {
                typeDeclaration.inductiveDeclaration = inductiveDeclaration;
            }
            return inductiveDeclaration;
        } else if (payload instanceof RefinerParser.Toplevel_type_declarationContext) {
            TypeDeclaration typeDeclaration = (TypeDeclaration) children.get(0);
            InductiveDeclaration inductiveDeclaration = new InductiveDeclaration(Arrays.asList(typeDeclaration));
            typeDeclaration.inductiveDeclaration = inductiveDeclaration;
            return inductiveDeclaration;
        } else if (payload instanceof RefinerParser.ParameterContext) {
            Symbol symbol = (Symbol) children.get(1);
            Expression type = (Expression) children.get(3);
            return new TypeDeclaration.Parameter(symbol, type);
        } else if (payload instanceof RefinerParser.Inductive_typeContext) {
            Symbol type = (Symbol) children.get(0);
            List<Expression> parameters = new ArrayList<>();
            for (int i = 1; i + 1 < children.size() && children.get(i + 1) instanceof Expression; i += 2) {
                parameters.add((Expression) children.get(i + 1));
            }
            return new InductiveType(type, parameters, new ArrayList<>());
        } else if (payload instanceof RefinerParser.Constructor_callContext) {
            InductiveType inductiveType = (InductiveType) children.get(0);
            Symbol constructor = (Symbol) children.get(2);
            return new ConstructorCall(inductiveType.inductiveType, inductiveType.parameters, constructor, new ArrayList<>());
        } else if (payload instanceof RefinerParser.MatchContext) {
            Expression expression = (Expression) children.get(1);
            Symbol expressionSymbol = (Symbol) children.get(3);
            List<Symbol> argumentSymbols = new ArrayList<>();

            int i = 5;

            for (; children.get(i) instanceof Symbol; i += 2) {
                argumentSymbols.add((Symbol) children.get(i));
            }

            i += i == 5 ? 2 : 1;
            Expression type = (Expression) children.get(i);
            i += 2;

            List<Match.Clause> clauses = new ArrayList<>();

            for (; i < children.size() - 1 && children.get(i) instanceof Match.Clause; i += 2) {
                clauses.add((Match.Clause) children.get(i));
            }

            return new Match(expression, expressionSymbol, argumentSymbols, type, clauses);
        } else if (payload instanceof RefinerParser.Match_clauseContext) {
            Symbol expressionSymbol = (Symbol) children.get(0);
            List<Symbol> argumentSymbols = new ArrayList<>();

            for (int i = 1; i < children.size() - 3 && children.get(i + 1) instanceof Symbol; i += 2) {
                argumentSymbols.add((Symbol) children.get(i + 1));
            }

            Expression expression = (Expression) children.get(children.size() - 1);

            return new Match.Clause(expressionSymbol, argumentSymbols, expression);
        } else if (payload instanceof RefinerParser.Fix_definitionContext) {
            Symbol name = (Symbol) children.get(0);
            Expression type = (Expression) children.get(2);
            Expression definition = (Expression) children.get(4);
            return new Definition(name, type, definition);
        } else if (payload instanceof RefinerParser.FixContext) {
            List<Fix.Clause> clauses = new ArrayList<>();
            for (int i = 2; i < children.size() - 3; i++) {
                Definition definition = (Definition) children.get(i);
                clauses.add(new Fix.Clause(
                        definition.name,
                        definition.type,
                        definition.definition
                ));
            }
            Symbol symbol = (Symbol) children.get(children.size() - 1);
            return new Fix(clauses, symbol);
        } else if (payload instanceof RefinerParser.Toplevel_fixContext) {
            Definition definition = (Definition) children.get(1);

            if (definition.definition == null) {
                System.err.println("top level fix can't have null definition");
                return null;
            }

            Fix.Clause clause = new Fix.Clause(
                    definition.name,
                    definition.type,
                    definition.definition
            );

            Fix fix = new Fix(
                    Arrays.asList(clause),
                    definition.name
            );
            return new Definition(
                    definition.name,
                    definition.type,
                    fix
            );
        } else if (payload instanceof RefinerParser.SortContext) {
            RefinerParser.SortContext context = (RefinerParser.SortContext) payload;
            String text = context.children.get(0).getText();

            if (text.equals("Prop")) {
                return new Prop();
            } else if (text.equals("Set")) {
                return new Set();
            } else if (text.matches("Type\\d+")) {
                int level = Integer.parseInt(text.substring(4));
                return new Type(level);
            }
        } else if(payload instanceof RefinerParser.OpenContext) {
            RefinerParser.OpenContext context = (RefinerParser.OpenContext) payload;
            return new Import(context.fileName.getText(), context.subSections.stream().map(
                    x -> x.getText()
            ).collect(Collectors.toList()));
        } else if(payload instanceof RefinerParser.SectionContext) {
            RefinerParser.SectionContext context = (RefinerParser.SectionContext) payload;
            return new Section(context.sectionName.getText(), (Program) children.get(3));
        }

        return null;
    }
}
