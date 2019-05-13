grammar Poulet;

@header {
    package poulet.parser;
}

program : (definition | print | type_declaration)+ ;

definition : symbol ':' expression (':=' expression)? ;

print : (REDUCE | CHECK | SCHOLIUMS) expression ;

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

PRINT_COMMAND : 'print' ;

SYMBOL : [a-zA-Z_][a-zA-Z0-9_]* ;

WHITESPACE : [ \t\r\n]+ -> skip ;

COMMENT : '/*' .*? ('*/' | EOF) -> skip ;

LINE_COMMENT : '//' ~[\r\n]* -> skip ;
