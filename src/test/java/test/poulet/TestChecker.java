package test.poulet;

import static org.junit.jupiter.api.Assertions.*;

import org.antlr.v4.runtime.CharStreams;
import org.junit.jupiter.api.Test;
import poulet.ast.Expression;
import poulet.ast.Program;
import poulet.ast.Symbol;
import poulet.ast.Variable;
import poulet.interpreter.Interpreter;
import poulet.parser.ASTParser;
import poulet.typing.Checker;
import poulet.typing.Context;
import poulet.typing.TypeException;

import java.util.Map;

public class TestChecker {
    private Expression parseExpression(String string) {
        Program program = ASTParser.parse(CharStreams.fromString("_:_:=" + string));
        Expression expression = Interpreter.getExpressions(program).get(0);
        return Interpreter.addIndices(expression);
    }

    @Test
    void testCheckType() {
        Context context = new Context(Map.of(
                new Symbol("int"), new Variable(new Symbol("*")),
                new Symbol("bool"), new Variable(new Symbol("*"))
        ));
        Expression term = parseExpression("\\x:int->x");
        Expression type = parseExpression("{_:int}bool");

        try {
            Checker.checkType(context, term, type);
            fail();
        } catch (TypeException e) {}
    }

    @Test
    void testCheckType2() {
        Context context = new Context(Map.of(
                new Symbol("int"), new Variable(new Symbol("*")),
                new Symbol("bool"), new Variable(new Symbol("*"))
        ));
        Expression term = parseExpression("\\x:int->x");
        Expression type = parseExpression("{_:int}int");

        try {
            Checker.checkType(context, term, type);
        } catch (TypeException e) {
            fail();
        }
    }

    @Test
    void testCheckType3() {
        Context context = new Context(Map.of(
                new Symbol("int"), new Variable(new Symbol("*"))
        ));
        Expression term = parseExpression("\\x:int->x");
        Expression type = parseExpression("{_:int}int");

        try {
            Checker.checkType(context, term, type);
        } catch (TypeException e) {
            fail();
        }
    }

    @Test
    void testCheckType4() {
        Context context = new Context(Map.of(
                new Symbol("int"), new Variable(new Symbol("*")),
                new Symbol("bool"), new Variable(new Symbol("*"))
        ));
        Expression term = parseExpression("\\x : int -> \\y : int -> y");
        Expression type = parseExpression("{_:int}{_:int}int");

        try {
            Checker.checkType(context, term, type);
        } catch (TypeException e) {
            fail();
        }
    }

    @Test
    void testCheckTypeWithSubstitution() throws Exception {
        Context context = new Context(Map.of(
                new Symbol("int"), new Variable(new Symbol("*")),
                new Symbol("bool"), new Variable(new Symbol("*"))
        ));
        Program actualProgram = ASTParser.parse(CharStreams.fromString("id : _ := \\x : int -> x\n_ : _ := \\x : int -> (id) x"));
        Program actualSubstitutedProgram = Interpreter.substituteCalls(actualProgram);
        Expression actualExpression = Interpreter.addIndices(Interpreter.getExpressions(actualSubstitutedProgram).get(1));
        Expression type = parseExpression("{_:int}int");

        try {
            Checker.checkType(context, actualExpression, type);
        } catch (TypeException e) {
            fail();
        }
    }
}
