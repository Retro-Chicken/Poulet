package test.poulet;

import org.antlr.v4.runtime.CharStreams;
import org.junit.jupiter.api.Test;
import poulet.ast.*;
import poulet.interpreter.Interpreter;
import poulet.parser.ASTParser;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestInterpreter {
    @Test
    void testSymbolIDs() {
        Program actualProgram = ASTParser.parse(CharStreams.fromString("_ : _ := \\x : _ -> (x) \\x : _ -> x"));
        Expression actualExpression = Interpreter.getExpressions(actualProgram).get(0);
        Expression actualWthIDs = Interpreter.addSymbolIDs(actualExpression);
        Expression expected = new Abstraction(
                new Symbol("x", 0),
                new Variable(new Symbol("_")),
                new Application(
                        new Variable(new Symbol("x", 0)),
                        new Abstraction(
                                new Symbol("x", 1),
                                new Variable(new Symbol("_")),
                                new Variable(new Symbol("x", 1))
                        )
                )
        );
        assertEquals(expected, actualWthIDs);
    }

    @Test
    void testEvaluation1() {
        Program actualProgram = ASTParser.parse(CharStreams.fromString("_ : _ := (\\x : _ -> x) (z) z"));
        Expression actualExpression = Interpreter.getExpressions(actualProgram).get(0);
        Expression actualResult = Interpreter.evaluateExpression(actualExpression);
        assertEquals(actualExpression, actualResult);
    }

    @Test
    void testEvaluation2() {
        Program actualProgram = ASTParser.parse(CharStreams.fromString("_ : _ := (\\x : _ -> x) z"));
        Expression actualExpression = Interpreter.getExpressions(actualProgram).get(0);
        Expression actualResult = Interpreter.evaluateExpression(actualExpression);
        Expression expected = new Variable(new Symbol("z"));
        assertEquals(expected, actualResult);
    }
}
