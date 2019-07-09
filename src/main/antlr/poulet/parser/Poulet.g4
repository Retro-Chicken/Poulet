grammar Poulet;

@header {
    package poulet.parser;
}

program : (definition | toplevel_fix | print | inductive_types | type_declaration | import_command | assert_eq)+ EOF ;

assert_eq : '#assert' expression '~' expression ;

definition : symbol ':' expression (':=' expression)? ;

toplevel_fix : 'fix' definition ;

print : (REDUCE | CHECK | SCHOLIUMS) expression ;

import_command : IMPORT STRING ;

inductive_types : 'inductive' '{' type_declaration* '}' ;

type_declaration : 'type' symbol parameter* ':' expression '{' constructor* '}' ;

parameter : '(' symbol ':' expression ')' ;

expression : sort | variable | abstraction | expression '(' (expression ',')* expression ')' | pi_type | match | inductive_type | constructor_call | fix | '(' expression ')' | string | character | <assoc=right> expression '->' expression ;

sort : 'Prop' | 'Set' | TYPE ;

TYPE : 'Type'[0-9]+ ;

constructor_call : inductive_type '.' symbol ;

inductive_type : symbol '[' ((expression ',')* expression)? ']' ;

variable : symbol ;

abstraction : '\\' symbol ':' expression IMPLICIT_ARGUMENT? '->' expression ;

pi_type : '{' symbol ':' expression '}' IMPLICIT_ARGUMENT? expression ;

constructor : symbol ':' expression ;

match : 'match' expression 'as' symbol '(' ((symbol ',')* symbol)? ')' 'in' expression '{' ((match_clause ',')* match_clause)? '}';

match_clause : symbol '(' ((symbol ',')* symbol)? ')' '=>' expression ;

fix : 'fix' '{' fix_definition* '}' '.' symbol ;

fix_definition : symbol ':' expression ':=' expression ;

symbol : SYMBOL ;

string : STRING ;

character : CHAR ;

REDUCE : '#reduce' ;

CHECK : '#check' ;

SCHOLIUMS : '#scholiums' ;

ASSERT : '#assert' ;

STRING : '"' (~'"')* '"' ;

CHAR : '\'' (~'\'')* '\'' ;

IMPLICIT_ARGUMENT : '?' ;

SYMBOL : [a-zA-Z_][a-zA-Z0-9_]* ;

INTEGER : [0-9]+ ;

WHITESPACE : [ \t\r\n]+ -> skip ;

COMMENT : '/*' .*? ('*/' | EOF) -> skip ;

LINE_COMMENT : '//' ~[\r\n]* -> skip ;

IMPORT : '#import' ;