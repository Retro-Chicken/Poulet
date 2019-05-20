package test.poulet;

import static org.junit.jupiter.api.Assertions.*;

import org.antlr.v4.runtime.CharStreams;
import org.junit.jupiter.api.Test;
import poulet.ast.Expression;
import poulet.ast.Program;
import poulet.ast.Symbol;
import poulet.ast.Variable;
import poulet.interpreter.DefinitionException;
import poulet.interpreter.Interpreter;
import poulet.parser.ASTParser;
import poulet.typing.Checker;
import poulet.typing.Environment;
import poulet.typing.TypeException;

import java.util.HashMap;
import java.util.Map;

public class TestChecker {
    private Expression parseExpression(String string) {
        Program program = ASTParser.parse(CharStreams.fromString("_:_:=" + string));
        Expression expression = Interpreter.getExpressions(program).get(0);
        return Interpreter.makeSymbolsUnique(expression);
    }
    @Test
    void testCheckType() {
        Environment environment = new Environment(Map.of(
                new Symbol("int"), new Variable(new Symbol("Type1")),
                new Symbol("bool"), new Variable(new Symbol("Type1"))
        ), new HashMap<>(), new HashMap<>());
        Expression term = parseExpression("\\x:int->x");
        Expression type = parseExpression("{_:int}bool");

        try {
            Checker.checkType(term, type, environment);
            fail();
        } catch (TypeException e) {}
    }

    @Test
    void testCheckType2() {
        Environment environment = new Environment(Map.of(
                new Symbol("int"), new Variable(new Symbol("Type1")),
                new Symbol("bool"), new Variable(new Symbol("Type1"))
        ), new HashMap<>(), new HashMap<>());
        Expression term = parseExpression("\\x:int->x");
        Expression type = parseExpression("{_:int}int");

        try {
            Checker.checkType(term, type, environment);
        } catch (TypeException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void testCheckType3() {
        Environment environment = new Environment(Map.of(
                new Symbol("int"), new Variable(new Symbol("Type1")),
                new Symbol("bool"), new Variable(new Symbol("Type1"))
        ), new HashMap<>(), new HashMap<>());
        Expression term = parseExpression("\\x:int->x");
        Expression type = parseExpression("{_:int}int");

        try {
            Checker.checkType(term, type, environment);
        } catch (TypeException e) {
            fail();
        }
    }

    @Test
    void testCheckType4() {
        Environment environment = new Environment(Map.of(
                new Symbol("int"), new Variable(new Symbol("Type1")),
                new Symbol("bool"), new Variable(new Symbol("Type1"))
        ), new HashMap<>(), new HashMap<>());
        Expression term = parseExpression("\\x : int -> \\y : int -> y");
        Expression type = parseExpression("{_:int}{_:int}int");

        try {
            Checker.checkType(term, type, environment);
        } catch (TypeException e) {
            fail();
        }
    }

    @Test
    void testCheckKind() {
        Environment environment = new Environment();
        Expression term = parseExpression("\\x:int->x");
        Expression type = parseExpression("{_:int}int");

        try {
            Checker.checkType(term, type, environment);
        } catch (TypeException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void testCheckTypeWithSubstitution() throws DefinitionException {
        Environment environment = new Environment(Map.of(
                new Symbol("int"), new Variable(new Symbol("Type1")),
                new Symbol("bool"), new Variable(new Symbol("Type1"))
        ), new HashMap<>(), new HashMap<>());
        Program actualProgram = ASTParser.parse(CharStreams.fromString("id : _ := \\x : int -> x\n_ : _ := \\x : int -> id(x)"));
        Program actualSubstitutedProgram = Interpreter.substituteCalls(actualProgram);
        Expression actualExpression = Interpreter.makeSymbolsUnique(Interpreter.getExpressions(actualSubstitutedProgram).get(1));
        Expression type = parseExpression("{_:int}int");
        type = Interpreter.makeSymbolsUnique(type);

        try {
            Checker.checkType(actualExpression, type, environment);
        } catch (TypeException e) {
            e.printStackTrace();
            fail();
        }
    }
}
