package poulet.interpreter;

import poulet.ast.*;
import poulet.typing.Checker;
import poulet.typing.Environment;

import java.io.PrintWriter;

public class Interpreter {
    public static void run(Program program, PrintWriter out) throws Exception {
        program = program.makeSymbolsUnique();
        Environment environment = new Environment();

        for (TopLevel topLevel : program.program) {
            if (topLevel instanceof Definition) {
                Definition definition = (Definition) topLevel;
                if (definition.definition != null)
                    environment = environment.appendScope(definition.name, definition.definition);
            } else if (topLevel instanceof InductiveDeclaration) {
                InductiveDeclaration inductiveDeclaration = (InductiveDeclaration) topLevel;
                Checker.checkInductiveDeclarationWellFormed(inductiveDeclaration, environment);
                environment = environment.appendInductive(inductiveDeclaration);
            }

        }

        for (TopLevel topLevel : program.program) {
            if (topLevel instanceof Definition) {
                Definition definition = (Definition) topLevel;
                if (definition.definition != null)
                    Checker.checkType(definition.definition, definition.type, environment);

                environment = environment.appendType(definition.name, definition.type);
            }
        }

        for (TopLevel topLevel : program.program) {
            try {
                if (topLevel instanceof Print) {
                    Print print = (Print) topLevel;
                    switch (print.command) {
                        case reduce:
                            Checker.deduceType(print.expression, environment);
                            out.println(Evaluator.reduce(print.expression, environment));
                            break;
                        case check:
                            out.println(Checker.deduceType(print.expression, environment)); //cleanCheck(Checker.deduceType(print.expression, context).expression(), 0));
                            break;
                    }
                } else if (topLevel instanceof Assertion) {
                    Assertion assertion = (Assertion) topLevel;
                    if (!Evaluator.convertible(assertion.a, assertion.b, environment)) {
                        throw new Exception("" + assertion + " failed");
                    }
                }
            } catch (Exception e) {
                System.err.println("Error on Line: " + topLevel);
                e.printStackTrace();
                throw new Exception();
            }
        }
    }
}
