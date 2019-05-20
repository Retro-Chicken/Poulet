grammar Poulet;

@header {
    package poulet.parser;
}

program : (definition | print | inductive_types | output | import_command)+ ;

definition : symbol ':' expression (':=' expression)? ;

print : (REDUCE | CHECK | SCHOLIUMS) expression ;

import_command : IMPORT STRING ;

output : OUTPUT_COMMAND STRING ;

inductive_types : 'inductive' '{' type_declaration* '}' ;

type_declaration : 'type' symbol parameter* ':' expression '{' constructor* '}' ;

parameter : '(' symbol ':' expression ')' ;

expression : (variable | abstraction | application | pi_type | match | inductive_type | constructor_call | fix) ;

constructor_call : inductive_type '.' symbol ;

inductive_type : symbol '[' ((expression ',')* expression)? ']' ;

variable : symbol ;

abstraction : '\\' symbol ':' expression '->' expression ;

application : '(' expression ')' expression ;

pi_type : '{' symbol ':' expression '}' expression ;

constructor : symbol ':' expression ;

match : 'match' expression 'as' symbol '(' ((symbol ',')* symbol)? ')' 'in' expression '{' ((match_clause ',')* match_clause)? '}';

match_clause : symbol '(' ((symbol ',')* symbol)? ')' '=>' expression ;

fix : 'fix' '{' fix_definition* '}' '.' symbol ;

fix_definition : symbol ':' expression ':=' expression ;

symbol : SYMBOL ;

REDUCE : '#reduce' ;

CHECK : '#check' ;

SCHOLIUMS : '#scholiums' ;

OUTPUT_COMMAND : '#print' ;

STRING : ('"' .*? '"') | ('\'' .*? '\'') ;

SYMBOL : [a-zA-Z_][a-zA-Z0-9_]* ;

INTEGER : [0-9]+ ;

WHITESPACE : [ \t\r\n]+ -> skip ;

COMMENT : '/*' .*? ('*/' | EOF) -> skip ;

LINE_COMMENT : '//' ~[\r\n]* -> skip ;

IMPORT : '#import' ;