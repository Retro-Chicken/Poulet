grammar Refiner;

@header {
    package poulet.parser.superficial;
}

program : (open | section | definition | toplevel_fix | inductive_types | toplevel_type_declaration | command)+ ;

section : 'Section' sectionName=SYMBOL '{' prgm=program '}' ;

open : 'open' fileName=SYMBOL ('.' subSections+=SYMBOL)* ;

definition : name=symbol ':' type=expression (':=' def=expression)? ;

toplevel_fix : 'fix' def=definition ;

command : REDUCE args+=expression | DEDUCE args+=expression | ASSERT args+=expression '~' args+=expression ;

inductive_types : 'inductive' '{' declarations+=type_declaration* '}' ;

toplevel_type_declaration : declaration=type_declaration ;

type_declaration : 'type' name=symbol parameters+=parameter* ':' type=expression '{' constructors+=constructor* '}' ;

parameter : '(' name=symbol ':' type=expression ')' ;

expression : sort #ExpSort
        | variable #ExpVariable
        | abstraction #ExpAbstraction
        | function=expression '(' (args+=expression ',')* args+=expression ')' #Application
        | pi_type #ExpPiType
        | match #ExpMatch
        | inductive_type #InductiveType
        | constructor_call #ConstructorCall
        | fix #ExpFix
        | '(' body=expression ')' #Parentheses
        | <assoc=right> domain=expression '->' codomain=expression #Function ;

sort : 'Prop' | 'Set' | TYPE ;

TYPE : 'Type'[0-9]+ ;

constructor_call : type=inductive_type '.' constructorName=symbol ;

inductive_type : typeName=symbol '[' ((parameters+=expression ',')* parameters+=expression)? ']' ;

variable : name=symbol ;

abstraction : '\\' name=symbol ':' type=expression '->' body=expression ;

pi_type : '{' name=symbol ':' type=expression '}' body=expression ;

constructor : name=symbol ':' type=expression ;

match : 'match' exp=expression 'as' name=symbol '(' ((argNames+=symbol ',')* argNames+=symbol)? ')' 'in' type=expression '{' ((clauses+=match_clause ',')* clauses+=match_clause)? '}' ;

match_clause : constructorName=symbol '(' ((argNames+=symbol ',')* argNames+=symbol)? ')' '=>' exp=expression ;

fix : 'fix' '{' definitions+=fix_definition* '}' '.' export=symbol ;

fix_definition : name=symbol ':' type=expression ':=' def=expression ;

symbol : SYMBOL ;

REDUCE : '#reduce' ;

DEDUCE : '#deduce' ;

ASSERT : '#assert' ;

STRING : '"' (~'"')* '"' ;

CHAR : '\'' (~'\'')* '\'' ;

SYMBOL : [a-zA-Z_][a-zA-Z0-9_]* ;

INTEGER : [0-9]+ ;

WHITESPACE : [ \t\r\n]+ -> skip ;