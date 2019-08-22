package poulet.kernel;

import poulet.PouletException;
import poulet.kernel.ast.*;
import poulet.kernel.context.GlobalContext;
import poulet.kernel.context.LocalContext;
import poulet.kernel.ast.TopLevel;

public class Kernel {
    private final GlobalContext globalContext;

    public Kernel() {
        globalContext = new GlobalContext();
    }

    public void runProgram(Program program) {
        Program unique = program.makeSymbolsUnique();
        //System.out.println(unique);

        for (TopLevel topLevel : unique.topLevels) {
            try {
                handleTopLevel(topLevel);
            } catch (PouletException e) {
                e.printStackTrace();
                System.err.println("\n on line: " + topLevel);
            }
        }
    }

    public void handleTopLevel(TopLevel topLevel) {
        if (topLevel instanceof Command) {
            runCommand((Command) topLevel);
        } else if (topLevel instanceof Definition) {
            Definition definition = (Definition) topLevel;
            if (definition.definition == null) {
                assume(definition.name, definition.type);
            } else {
                define(definition.name, definition.type, definition.definition);
            }
        } else if (topLevel instanceof InductiveDeclaration) {
            declareInductive((InductiveDeclaration) topLevel);
        }
    }

    private void runCommand(Command command) {
        if (command.action == Command.Action.ASSERT) {
            Expression a = command.arguments.get(0);
            Expression b = command.arguments.get(1);

            if (!convertible(a, b)) {
                throw new PouletException("assertion " + a + " ~ " + b + " failed");
            }
        } else if (command.action == Command.Action.DEDUCE) {
            Expression term = command.arguments.get(0);
            System.out.println(term + " : " + reduce(deduceType(term)));
        } else if (command.action == Command.Action.REDUCE) {
            Expression term = command.arguments.get(0);
            System.out.println(term + " ▹ " + reduce(term));
        }
    }

    public void declareInductive(InductiveDeclaration inductiveDeclaration) {
        Checker.checkWellFormed(inductiveDeclaration);
        globalContext.declareInductive(inductiveDeclaration);
    }

    public void assume(Symbol name, Expression type) {
        Expression sort = deduceType(type);

        if (sort instanceof Sort) {
            globalContext.assume(name, type);
        } else {
            throw new PouletException("type of assumption must have a sort");
        }
    }

    public void define(Symbol name, Expression type, Expression definition) {
        checkType(definition, type);
        globalContext.define(name, type, definition);
    }

    public Expression reduce(Expression term) {
        LocalContext emptyContext = new LocalContext(globalContext);
        return reduce(term, emptyContext);
    }

    public Expression reduce(Expression term, LocalContext context) {
        return Reducer.reduce(term, context);
    }

    public boolean convertible(Expression a, Expression b) {
        LocalContext emptyContext = new LocalContext(globalContext);
        return Reducer.convertible(a, b, emptyContext);
    }

    public boolean convertible(Expression a, Expression b, LocalContext context) {
        return Reducer.convertible(a, b, context);
    }

    public void checkType(Expression term, Expression type) {
        LocalContext emptyContext = new LocalContext(globalContext);
        checkType(term, type, emptyContext);
    }

    public void checkType(Expression term, Expression type, LocalContext context) {
        Checker.checkType(term, type, context);
    }

    public Expression deduceType(Expression term) {
        LocalContext emptyContext = new LocalContext(globalContext);
        return deduceType(term, emptyContext);
    }

    public Expression deduceType(Expression term, LocalContext context) {
        return Checker.deduceType(term, context);
    }
}
