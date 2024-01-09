grammar GitConfig;

gitconfig
    : (section | EOL)* EOF
    ;

section
    : sectionHeader line*
    ;

sectionHeader
    : '[' string ']' EOL
    | '[' string string ']' EOL
    ;

string
    : CHARS
    | STRING
    ;

line
    : stringList ('=' stringList)? EOL
    ;

stringList
    : string (',' string?)*
    ;

CHARS
    : (
        'A' .. 'Z'
        | '0' .. '9'
        | 'a' .. 'z'
        | '.'
        | '%'
        | '"'
        | '\\'
        | '/'
        | '*'
        | '@'
        | '&'
        | '_'
        | '{'
        | '}'
        | '<'
        | '>'
        | '-'
        | ':'
        | '~'
    )+
    ;

STRING
    : '"' (~ ('"' | '\n'))* '"'
    ;

COMMENT
    : (';' | '#') ~ [\r\n]* EOL -> skip
    ;

EOL
    : [\r\n]+
    ;

WS
    : [ \t]+ -> skip
    ;