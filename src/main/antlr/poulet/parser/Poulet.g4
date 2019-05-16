grammar Poulet;

@header {
    package poulet.parser;
}

program : (definition | print | inductive_type | output)+ ;

definition : symbol ':' expression (':=' expression)? ;

print : (REDUCE | CHECK | SCHOLIUMS) expression ;

output : OUTPUT_COMMAND STRING ;

inductive_type : 'inductive' INTEGER '{' type_declaration* '}' ;

type_declaration : 'type' symbol ':' expression '{' constructor* '}' ;

expression : (variable | abstraction | application | pi_type) ;

variable : (symbol '.')? symbol ;

abstraction : '\\' symbol ':' expression '->' expression ;

application : '(' expression ')' expression ;

pi_type : '{' symbol ':' expression '}' expression ;

constructor : symbol ':' expression ;

symbol : SYMBOL ;

REDUCE : '#reduce' ;

CHECK : '#check' ;

SCHOLIUMS : '#scholiums' ;

OUTPUT_COMMAND : 'print' ;

STRING : ('"' .*? '"') | ('\'' .*? '\'') ;

INTEGER : [0-9]+ ;

SYMBOL : [a-zA-Z_][a-zA-Z0-9_]* ;

WHITESPACE : [ \t\r\n]+ -> skip ;

COMMENT : '/*' .*? ('*/' | EOF) -> skip ;

LINE_COMMENT : '//' ~[\r\n]* -> skip ;
