package test.poulet;

import org.antlr.v4.runtime.CharStreams;
import org.junit.jupiter.api.Test;
import poulet.ast.*;
import poulet.interpreter.Interpreter;
import poulet.parser.ASTParser;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class TestInterpreter {
    @Test
    void testSymbolIDs() {
        Program actualProgram = ASTParser.parse(CharStreams.fromString("_ : _ := \\x : _ -> (x) \\x : _ -> x"));
        Expression actualExpression = Interpreter.getExpressions(actualProgram).get(0);
        Expression actualWthIDs = Interpreter.addIndices(actualExpression);
        Expression expected = new Abstraction(
                null,
                new Variable(new Symbol("_")),
                new Application(
                        new Variable(new Symbol(0)),
                        new Abstraction(
                                null,
                                new Variable(new Symbol("_")),
                                new Variable(new Symbol(0))
                        )
                )
        );
        assertEquals(expected, actualWthIDs);
    }

    @Test
    void testSymbolIDs2() {
        Program actualProgram = ASTParser.parse(CharStreams.fromString("_ : _ := \\x : _ -> (x) \\y : _ -> x"));
        Expression actualExpression = Interpreter.getExpressions(actualProgram).get(0);
        Expression actualWthIDs = Interpreter.addIndices(actualExpression);
        Expression expected = new Abstraction(
                null,
                new Variable(new Symbol("_")),
                new Application(
                        new Variable(new Symbol( 0)),
                        new Abstraction(
                                null,
                                new Variable(new Symbol("_")),
                                new Variable(new Symbol(1))
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
        Program actualProgram = ASTParser.parse(CharStreams.fromString("id : _ := \\x : _ -> x\n#reduce \\x : _ -> x\nid2 : _ := \\x : _ -> id\n_ : _ := ((id2) w) z)"));
        Program actualSubstitutedProgram = Interpreter.substituteCalls(actualProgram);
        Expression actualExpression = Interpreter.getExpressions(actualSubstitutedProgram).get(2);
        Expression actualResult = Interpreter.evaluateExpression(actualExpression);
        Expression expected = new Variable(new Symbol("z"));
        assertEquals(expected, actualResult);
    }

    @Test
    void testRecursion() throws Exception {
        Program actualProgram = ASTParser.parse(CharStreams.fromFileName("test/recursion_test.poulet"));
        Program actualSubstitutedProgram = Interpreter.substituteCalls(actualProgram);
        List<Expression> actualExpressions = Interpreter.getExpressions(actualSubstitutedProgram);
        Expression actualResult = Interpreter.evaluateExpression(actualExpressions.get(actualExpressions.size() - 1));
        System.out.println(">>> " + actualResult);
    }

    @Test
    void testEvalWithIndices() throws Exception {
        Program actualProgram = ASTParser.parse(CharStreams.fromString("_:_:=((\\x:_->\\y:_->x)a)b"));
        actualProgram = Interpreter.substituteCalls(actualProgram);
        List<Expression> expressions = Interpreter.getExpressions(actualProgram);
        Expression expression = expressions.get(expressions.size() - 1);
        expression = Interpreter.addIndices(expression);
        Expression output = Interpreter.evaluateExpression(expression);
        Expression expected = new Variable(new Symbol("a"));
        assertEquals(expected, output);
    }

    @Test
    void substituteFunctionCalls2() {
        try {
            Program actualProgram = ASTParser.parse(CharStreams.fromString("func : _ := \\x : _ -> z\nfunc2 : _ := \\z : _ -> (func) z\n_ : _ := (func2) w"));
            Program actualSubstitutedProgram = Interpreter.substituteCalls(actualProgram);
            Expression actualExpression = Interpreter.getExpressions(actualSubstitutedProgram).get(2);
            Expression actualResult = Interpreter.evaluateExpression(actualExpression);

            try {
                Variable variable = (Variable) actualResult;
                if (!variable.symbol.toString().equals("z"))
                    fail();
            } catch (Exception e) {
                fail();
            }
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    void transformProgram() {
        try {
            Program actualProgram = ASTParser.parse(CharStreams.fromString("func : _ := \\x : _ -> z\nfunc2 : _ := \\z : _ -> (func) z\n_ : _ := (func2) w"));
            Program transformed = Interpreter.transform(actualProgram);
            Expression transformedExpression = Interpreter.getExpressions(transformed).get(2);
            Expression evaluated = Interpreter.evaluateExpression(transformedExpression);
            Variable variable = (Variable) evaluated;
            if(!variable.symbol.toString().equals("z"))
                fail();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
}
