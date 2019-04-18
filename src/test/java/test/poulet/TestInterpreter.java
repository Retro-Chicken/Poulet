package test.poulet;

import org.antlr.v4.runtime.CharStreams;
import org.junit.jupiter.api.Test;
import poulet.ast.*;
import poulet.interpreter.Interpreter;
import poulet.parser.ASTParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestInterpreter {
    @Test
    void testSymbolIDs() {
        Program actualProgram = ASTParser.parse(CharStreams.fromString("_ : _ := \\x : _ -> (x) \\x : _ -> x"));
        Expression actualExpression = Interpreter.getExpressions(actualProgram).get(0);
        Expression actualWthIDs = Interpreter.addSymbolIDs(actualExpression);
        Expression expected = new Abstraction(
                new Symbol("x", 17),
                new Variable(new Symbol("_")),
                new Application(
                        new Variable(new Symbol("x", 17)),
                        new Abstraction(
                                new Symbol("x", 18),
                                new Variable(new Symbol("_")),
                                new Variable(new Symbol("x", 18))
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

    @Test
    void substituteFunctionCalls() throws Exception {
        Program actualProgram = ASTParser.parse(CharStreams.fromString("id : _ := \\x : _ -> x\nid2 : _ := \\x : _ -> id\n_ : _ := ((id2) w) z)"));
        Program actualSubstitutedProgram = Interpreter.substituteCalls(actualProgram);
        Expression actualExpression = Interpreter.getExpressions(actualSubstitutedProgram).get(2);
        Expression actualResult = Interpreter.evaluateExpression(actualExpression);
        Expression expected = new Variable(new Symbol("z"));
        assertEquals(expected, actualResult);
    }
}
