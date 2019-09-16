package poulet.parser;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import poulet.parser.superficial.SuperficialBaseVisitor;
import poulet.parser.superficial.SuperficialLexer;
import poulet.parser.superficial.SuperficialParser;
import poulet.superficial.ast.inlines.LetIn;
import poulet.superficial.ast.inlines.Where;
import poulet.superficial.ast.multilines.Import;
import poulet.superficial.ast.Program;
import poulet.superficial.ast.multilines.Proof;
import poulet.superficial.ast.multilines.Section;
import poulet.superficial.ast.expressions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SuperficialASTParser extends SuperficialBaseVisitor<SuperficialNode> {
    public static Program parse(CharStream stream) {
        SuperficialLexer lexer = new SuperficialLexer(stream);
        SuperficialParser parser = new SuperficialParser(new CommonTokenStream(lexer));
        return new SuperficialASTParser().visitProgram(parser.program());
    }

    @Override
    public Program visitProgram(SuperficialParser.ProgramContext ctx) {
        List<SuperficialNode> nodes = new ArrayList<>();
        for(ParseTree node : ctx.children) {
            nodes.add(this.visit(node));
        }
        return new Program(nodes);
    }

    @Override
    public Section visitSection(SuperficialParser.SectionContext ctx) {
        return new Section(ctx.sectionName.getText(), this.visitProgram(ctx.prgm));
    }

    @Override
    public Import visitOpen(SuperficialParser.OpenContext ctx) {
        return new Import(ctx.fileName.getText(), ctx.subSections.stream().map(Token::getText).collect(Collectors.toList()));
    }

    @Override
    public Definition visitDefinition(SuperficialParser.DefinitionContext ctx) {
        if(ctx.def == null)
            return new Definition(this.visitSymbol(ctx.name), this.visitExpression(ctx.type));
        else
            return new Definition(this.visitSymbol(ctx.name), this.visitExpression(ctx.type), this.visitExpression(ctx.def));
    }

    @Override
    public Definition visitToplevel_fix(SuperficialParser.Toplevel_fixContext ctx) {
        Definition definition = this.visitDefinition(ctx.def);

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
    }

    @Override
    public Command visitCommand(SuperficialParser.CommandContext ctx) {
        List<Expression> arguments = ctx.args.stream().map(this::visitExpression).collect(Collectors.toList());

        if (ctx.REDUCE() != null) {
            return new Command(
                Command.Action.REDUCE,
                arguments
            );
        } else if (ctx.DEDUCE() != null) {
            return new Command(
                Command.Action.DEDUCE,
                arguments
            );
        } else if (ctx.ASSERT() != null) {
            return new Command(
                Command.Action.ASSERT,
                arguments
            );
        }

        return null;
    }

    @Override
    public InductiveDeclaration visitInductive_types(SuperficialParser.Inductive_typesContext ctx) {
        List<TypeDeclaration> declarations = ctx.declarations.stream().map(this::visitType_declaration).collect(Collectors.toList());
        InductiveDeclaration inductiveDeclaration = new InductiveDeclaration(declarations);
        for (TypeDeclaration typeDeclaration : inductiveDeclaration.typeDeclarations) {
            typeDeclaration.inductiveDeclaration = inductiveDeclaration;
        }
        return inductiveDeclaration;
    }

    @Override
    public InductiveDeclaration visitToplevel_type_declaration(SuperficialParser.Toplevel_type_declarationContext ctx) {
        TypeDeclaration typeDeclaration = this.visitType_declaration(ctx.declaration);
        InductiveDeclaration inductiveDeclaration = new InductiveDeclaration(Arrays.asList(typeDeclaration));
        typeDeclaration.inductiveDeclaration = inductiveDeclaration;
        return inductiveDeclaration;
    }

    @Override
    public TypeDeclaration visitType_declaration(SuperficialParser.Type_declarationContext ctx) {
        Symbol name = this.visitSymbol(ctx.name);
        List<TypeDeclaration.Parameter> parameters = ctx.parameters.stream().map(this::visitParameter).collect(Collectors.toList());
        List<TypeDeclaration.Constructor> constructors = ctx.constructors.stream().map(this::visitConstructor).collect(Collectors.toList());
        Expression type = this.visitExpression(ctx.type);
        TypeDeclaration typeDeclaration = new TypeDeclaration(null, name, parameters, type, constructors);
        return typeDeclaration;
    }

    public Expression visitExpression(SuperficialParser.ExpressionContext ctx) {
        return (Expression) this.visit(ctx);
    }

    @Override
    public TypeDeclaration.Parameter visitParameter(SuperficialParser.ParameterContext ctx) {
        Symbol symbol = this.visitSymbol(ctx.name);
        Expression type = this.visitExpression(ctx.type);
        return new TypeDeclaration.Parameter(symbol, type);
    }

    @Override
    public Prod visitExpPiType(SuperficialParser.ExpPiTypeContext ctx) {
        return this.visitPi_type(ctx.pi_type());
    }

    @Override
    public Prod visitPi_type(SuperficialParser.Pi_typeContext ctx) {
        return new Prod(this.visitSymbol(ctx.name), this.visitExpression(ctx.type), this.visitExpression(ctx.body));
    }

    @Override
    public Prod visitFunction(SuperficialParser.FunctionContext ctx) {
        return new Prod(new Symbol("_"), this.visitExpression(ctx.domain), this.visitExpression(ctx.codomain));
    }

    @Override
    public InductiveType visitInductiveType(SuperficialParser.InductiveTypeContext ctx) {
        return this.visitInductive_type(ctx.inductive_type());
    }

    @Override
    public ConstructorCall visitConstructorCall(SuperficialParser.ConstructorCallContext ctx) {
        return this.visitConstructor_call(ctx.constructor_call());
    }

    @Override
    public Application visitApplication(SuperficialParser.ApplicationContext ctx) {
        Expression function = this.visitExpression(ctx.function);
        List<Expression> arguments = ctx.args.stream().map(this::visitExpression).collect(Collectors.toList());
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
    }

    @Override
    public Expression visitParentheses(SuperficialParser.ParenthesesContext ctx) {
        return this.visitExpression(ctx.body);
    }

    @Override
    public Sort visitSort(SuperficialParser.SortContext ctx) {
        String text = ctx.children.get(0).getText();

        if (text.equals("Prop")) {
            return new Prop();
        } else if (text.equals("Set")) {
            return new Set();
        } else if (text.matches("Type\\d+")) {
            int level = Integer.parseInt(text.substring(4));
            return new Type(level);
        }

        return null;
    }

    @Override
    public ConstructorCall visitConstructor_call(SuperficialParser.Constructor_callContext ctx) {
        InductiveType inductiveType = this.visitInductive_type(ctx.type);
        Symbol constructor = this.visitSymbol(ctx.constructorName);
        return new ConstructorCall(inductiveType.inductiveType, inductiveType.parameters, constructor, new ArrayList<>());
    }

    @Override
    public InductiveType visitInductive_type(SuperficialParser.Inductive_typeContext ctx) {
        Symbol type = this.visitSymbol(ctx.typeName);
        List<Expression> parameters = ctx.parameters.stream().map(this::visitExpression).collect(Collectors.toList());
        return new InductiveType(type, parameters, new ArrayList<>());
    }

    @Override
    public Var visitVariable(SuperficialParser.VariableContext ctx) {
        return new Var(this.visitSymbol(ctx.name));
    }

    @Override
    public Fix visitExpFix(SuperficialParser.ExpFixContext ctx) {
        return this.visitFix(ctx.fix());
    }

    @Override
    public Var visitExpVariable(SuperficialParser.ExpVariableContext ctx) {
        return this.visitVariable(ctx.variable());
    }

    @Override
    public Abstraction visitExpAbstraction(SuperficialParser.ExpAbstractionContext ctx) {
        return this.visitAbstraction(ctx.abstraction());
    }

    @Override
    public Sort visitExpSort(SuperficialParser.ExpSortContext ctx) {
        return this.visitSort(ctx.sort());
    }

    @Override
    public Match visitExpMatch(SuperficialParser.ExpMatchContext ctx) {
        return this.visitMatch(ctx.match());
    }

    @Override
    public Abstraction visitAbstraction(SuperficialParser.AbstractionContext ctx) {
        return new Abstraction(this.visitSymbol(ctx.name), this.visitExpression(ctx.type), this.visitExpression(ctx.body));
    }

    @Override
    public TypeDeclaration.Constructor visitConstructor(SuperficialParser.ConstructorContext ctx) {
        Symbol name = this.visitSymbol(ctx.name);
        Expression definition = this.visitExpression(ctx.type);
        return new TypeDeclaration.Constructor(name, definition);
    }

    @Override
    public Match visitMatch(SuperficialParser.MatchContext ctx) {
        Expression expression = this.visitExpression(ctx.exp);
        Symbol expressionSymbol = this.visitSymbol(ctx.name);
        List<Symbol> argumentSymbols = ctx.argNames.stream().map(this::visitSymbol).collect(Collectors.toList());
        Expression type = this.visitExpression(ctx.type);
        List<Match.Clause> clauses = ctx.clauses.stream().map(this::visitMatch_clause).collect(Collectors.toList());

        return new Match(expression, expressionSymbol, argumentSymbols, type, clauses);
    }

    @Override
    public Match.Clause visitMatch_clause(SuperficialParser.Match_clauseContext ctx) {
        Symbol expressionSymbol = this.visitSymbol(ctx.constructorName);
        List<Symbol> argumentSymbols = ctx.argNames.stream().map(this::visitSymbol).collect(Collectors.toList());
        Expression expression = this.visitExpression(ctx.exp);

        return new Match.Clause(expressionSymbol, argumentSymbols, expression);
    }

    @Override
    public Fix visitFix(SuperficialParser.FixContext ctx) {
        List<Fix.Clause> clauses = ctx.definitions.stream().map(def -> {
            Definition definition = this.visitFix_definition(def);
            return new Fix.Clause(
                    definition.name,
                    definition.type,
                    definition.definition
            );
        }).collect(Collectors.toList());
        Symbol symbol = this.visitSymbol(ctx.export);
        return new Fix(clauses, symbol);
    }

    @Override
    public Definition visitFix_definition(SuperficialParser.Fix_definitionContext ctx) {
        return new Definition(this.visitSymbol(ctx.name), this.visitExpression(ctx.type), this.visitExpression(ctx.def));
    }

    @Override
    public Symbol visitSymbol(SuperficialParser.SymbolContext ctx) {
        return new Symbol(ctx.getText());
    }

    @Override
    public Where visitWhere(SuperficialParser.WhereContext ctx) {
        return new Where(this.visitSymbol(ctx.name), this.visitExpression(ctx.value), this.visitExpression(ctx.body));
    }

    @Override
    public LetIn visitLetIn(SuperficialParser.LetInContext ctx) {
        return new LetIn(this.visitSymbol(ctx.name), this.visitExpression(ctx.value), this.visitExpression(ctx.body));
    }

    @Override
    public Proof visitProof(SuperficialParser.ProofContext ctx) {
        return new Proof(
                this.visitSymbol(ctx.name),
                this.visitExpression(ctx.type),
                this.visitExpression(ctx.def),
                ctx.lemmas.stream().map(this::visitDefinition).collect(Collectors.toList())
        );
    }
}
