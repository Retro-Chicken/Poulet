grammar Vernacular;

@header {
    package poulet.parser.vernacular;
}

program : (definition | toplevel_fix | inductive_types | toplevel_type_declaration | command | assert_eq)+ EOF ;

let_in : 'let' symbol (':' expression)? ':=' expression 'in' expression ;

assert_eq : '#assert' expression '~' expression ;

definition : symbol ':' expression (':=' expression)? ;

toplevel_fix : 'fix' definition ;

command : REDUCE expression | DEDUCE expression | ASSERT expression '~' expression ;

inductive_types : 'inductive' '{' type_declaration* '}' ;

toplevel_type_declaration : type_declaration ;

type_declaration : 'type' symbol parameter* ':' expression '{' constructor* '}' ;

parameter : '(' symbol ':' expression ')' ;

expression : let_in | sort | variable | abstraction | expression '(' (expression ',')* expression ')' | pi_type | match | inductive_type | constructor_call | fix | '(' expression ')' | <assoc=right> expression '->' expression ;

sort : 'Prop' | 'Set' | TYPE ;

TYPE : 'Type'[0-9]+ ;

constructor_call : inductive_type '.' symbol ;

inductive_type : symbol '[' ((expression ',')* expression)? ']' ;

variable : symbol ;

abstraction : '\\' symbol (':' expression)? '->' expression ;

pi_type : '{' symbol ':' expression '}' expression ;

constructor : symbol ':' expression ;

match : 'match' expression 'as' symbol '(' ((symbol ',')* symbol)? ')' 'in' expression '{' ((match_clause ',')* match_clause)? '}';

match_clause : symbol '(' ((symbol ',')* symbol)? ')' '=>' expression ;

fix : 'fix' '{' fix_definition* '}' '.' symbol ;

fix_definition : symbol ':' expression ':=' expression ;

symbol : SYMBOL ;

REDUCE : '#reduce' ;

DEDUCE : '#deduce' ;

ASSERT : '#assert' ;

STRING : '"' (~'"')* '"' ;

CHAR : '\'' (~'\'')* '\'' ;

SYMBOL : [a-zA-Z_][a-zA-Z0-9_]* ;

INTEGER : [0-9]+ ;

WHITESPACE : [ \t\r\n]+ -> skip ;
