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
import poulet.typing.Context;
import poulet.typing.TypeException;
import poulet.value.NFree;
import poulet.value.VNeutral;

import java.util.Map;

public class TestChecker {
    private Expression parseExpression(String string) {
        Program program = ASTParser.parse(CharStreams.fromString("_:_:=" + string));
        Expression expression = Interpreter.getExpressions(program).get(0);
        return Interpreter.addIndices(expression);
    }
    private VNeutral vFree(String name) {
        return new VNeutral(new NFree(new Symbol(name)));
    }

    @Test
    void testCheckType() {
        Context context = new Context(Map.of(
                new Symbol("int"), vFree("Type1"),
                new Symbol("bool"), vFree("Type1")
        ));
        Expression term = parseExpression("\\x:int->x");
        Expression type = parseExpression("{_:int}bool");

        try {
            Checker.checkType(term, Interpreter.evaluateExpression(type), context);
            fail();
        } catch (TypeException e) {}
    }

    @Test
    void testCheckType2() {
        Context context = new Context(Map.of(
                new Symbol("int"), vFree("Type1"),
                new Symbol("bool"), vFree("Type1")
        ));
        Expression term = parseExpression("\\x:int->x");
        Expression type = parseExpression("{_:int}int");

        try {
            Checker.checkType(term, Interpreter.evaluateExpression(type), context);
        } catch (TypeException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void testCheckType3() {
        Context context = new Context(Map.of(
                new Symbol("int"), vFree("Type1")
        ));
        Expression term = parseExpression("\\x:int->x");
        Expression type = parseExpression("{_:int}int");

        try {
            Checker.checkType(term, Interpreter.evaluateExpression(type), context);
        } catch (TypeException e) {
            fail();
        }
    }

    @Test
    void testCheckType4() {
        Context context = new Context(Map.of(
                new Symbol("int"), vFree("Type1"),
                new Symbol("bool"), vFree("Type1")
        ));
        Expression term = parseExpression("\\x : int -> \\y : int -> y");
        Expression type = parseExpression("{_:int}{_:int}int");

        try {
            Checker.checkType(term, Interpreter.evaluateExpression(type), context);
        } catch (TypeException e) {
            fail();
        }
    }

    @Test
    void testCheckKind() {
        Context context = new Context();
        Expression term = parseExpression("\\x:int->x");
        Expression type = parseExpression("{_:int}int");

        try {
            Checker.checkType(term, Interpreter.evaluateExpression(type), context);
            fail();
        } catch (TypeException e) { }
    }

    @Test
    void testCheckTypeWithSubstitution() throws DefinitionException {
        Context context = new Context(Map.of(
                new Symbol("int"), vFree("Type1"),
                new Symbol("bool"), vFree("Type1")
        ));
        Program actualProgram = ASTParser.parse(CharStreams.fromString("id : _ := \\x : int -> x\n_ : _ := \\x : int -> (id) x"));
        Program actualSubstitutedProgram = Interpreter.substituteCalls(actualProgram);
        Expression actualExpression = Interpreter.addIndices(Interpreter.getExpressions(actualSubstitutedProgram).get(1));
        Expression type = parseExpression("{_:int}int");

        try {
            Checker.checkType(actualExpression, Interpreter.evaluateExpression(type), context);
        } catch (TypeException e) {
            fail();
        }
    }
}
