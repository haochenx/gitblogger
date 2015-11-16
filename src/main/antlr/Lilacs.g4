grammar Lilacs;

NUM_INF   : 'Infinity' ;
NUM_NAN   : 'NaN' ;

C_UNDEFINED : '#undefined' ;
C_NULL      : '#null' ;
C_TRUE      : '#t' ;
C_FALSE     : '#f' ;

KW_EXACT : 'exact' ;
KW_NEW : 'new' ;
KW_CASE : 'case' ;
KW_WITH : 'w%' ;
KW_THROW : 'throw%' ;
KW_LET   : 'let' ;
KW_BEGIN : 'begin' ;
KW_FOR   : 'for' ;
KW_FORIN : 'for-in' ;
KW_ARROW : '=>' ;
KW_RAWSEQ_START : '::!';
KW_RAWSEQ_ENDS : '\n!::';

NUM_INT     : [+\-]? ([1-9] [0-9]* | '0') ;
NUM_DECIMAL : NUM_INT '.' NUM_INT? | '0'? '.' NUM_INT ;
NUM_SCI     : (NUM_DECIMAL | NUM_INT) [eE] NUM_INT ;
NUM_OCT     : '0' [0-9]+ ;
NUM_HEX     : '0x' [0-9a-fA-F]+ ;

RAWSEQ : KW_RAWSEQ_START .*? '\n' .*? KW_RAWSEQ_ENDS ;

OP
    : '+' | '-' | '*' | '/' | '%' | '>' | '>=' | '<' | '<=' | '&&' | '||' | '&'
    | '|' | '^' | '!' | '<<' | '>>' | '>>>' | 'eq?' | 'eq??' | 'neq?' | 'neq??'
    | '<-'
    ;

ID  : [a-zA-Z$] [a-zA-Z$0-9\-:]* ;
TAG : ':' [a-zA-Z$] [a-zA-Z$0-9\-:]* ;
WP  : [ \n\r\t]+ ;

LITSEQ : '\"' LITCHAR* '\"' ;
LITCHAR : LITCHAR_XESP | LITCHAR_ESP ;
LITCHAR_XESP : ~('\u0000'..'\u001f' | '\u007f' | '\\' | '\"') ;
// TODO support HexEscapeSequence and UnicodeEscapeSequence
LITCHAR_ESP : '\\' ESPCHAR ;
ESPCHAR : '\'' | '\"' | '\\' | 'b' | 'f' | 'n' | 'r' | 't' | 'v' | '0' | '\n' ;

c_undefined : C_UNDEFINED ;
c_null      : C_NULL      ;
c_true      : C_TRUE      ;
c_false     : C_FALSE     ;

exp : xioexp | ioexp ;

xioexp : sexp | constant | number | obj_cons | arr_cons | string ;

tag : TAG ;

op : OP ;

number : number_int | number_decimal | number_sci | number_oct | number_hex | number_constant ;

number_int : NUM_INT ;

number_decimal : NUM_DECIMAL ;

number_sci : NUM_SCI ;

number_oct : NUM_OCT ;

number_hex : NUM_HEX ;

number_constant : NUM_INF | NUM_NAN ;

string : string_quoted | string_raw ;

string_quoted : LITSEQ ;

string_raw : RAWSEQ ;

exps
    : exp
    | exp WP exps
    ;

id : ID ;

ids
    : id
    | id WP ids
    ;

ioexp
    : id
    | xioexp '/' id
    | ioexp '/' id
    ;

sexp : t_sexp | f_sexp ;

t_sexp : sexp_invk | sexp_sf | sexp_sugar ;

sexp_sf : sf_if | sf_exact | sf_new | sf_case | sf_aat | sf_with | sf_throw ;

sexp_sugar : sugar_let | sugar_begin | sugar_for | sugar_forin | sugar_shat ;

sf_if
    : '(' WP? 'if' WP exp WP exp WP exp  WP? ')'
    ;

sf_exact
    : '(' WP? KW_EXACT WP ioexp WP? ')'
    ;

sf_new
    : '(' WP? KW_NEW WP exp (WP exps)? WP? ')'
    ;

sf_case
    : '(' WP? KW_CASE WP exp WP sf_case_cases WP? ')'
    ;

sf_case_normal_case
    : '(' WP? '(' WP? exps WP? ')' WP exps WP? ')'
    ;

sf_case_else_case
    : '(' WP? 'else' WP exps WP? ')'
    ;

sf_case_cases
    : sf_case_normal_case (WP sf_case_normal_case)*
    | (sf_case_normal_case WP)* sf_case_else_case
    ;

sf_with
    : '(' WP? KW_WITH WP '(' WP? exp WP? ')' WP exps WP? ')'
    | '(' WP? KW_WITH WP '(' WP? exp WP exp WP? ')' WP exps WP? ')'
    ;

sf_throw
    : '(' WP? KW_THROW WP exp WP? ')'
    ;

sf_aat
    : '(' WP? '@@' WP exp WP exp WP? ')'
    ;

sugar_shat
    : '(' WP? '@' id (WP obj_cons_kvl)? WP '::' WP exps WP? ')'
    | '(' WP? exp '/' '@' id (WP obj_cons_kvl)? WP '::' WP exps WP? ')'
    ;

sugar_let
    : '(' WP? KW_LET WP '(' WP? binds  WP? ')' WP exps WP? ')'
    ;

sugar_begin
    : '(' WP? KW_BEGIN WP exps WP? ')'
    ;

sugar_for
    : '(' WP? KW_FOR WP bind WP exps WP? ')'
    ;

sugar_forin
    : '(' WP? KW_FORIN WP bind WP exps WP? ')'
    ;

bind
    : '(' WP? id WP exp WP? ')'
    ;

binds
    : bind
    | bind WP binds
    ;

sexp_invk
    : '(' WP? exp (WP exps)? WP? ')'
    | '(' WP? op (WP exps)? WP? ')'
    ;

constant : c_undefined | c_null | c_true | c_false ;

f_sexp
    : '(' WP? ids WP? KW_ARROW WP? exps WP? ')'
    | '(' WP? '[' WP? id WP? ']' WP? ids WP? KW_ARROW WP? exps WP? ')'
    ;

arr_cons
    : '[' WP? ']'
    | '[' WP? exps  WP? ']'
    ;

obj_cons
    : '{' WP? '}'
    | '{' WP? obj_cons_kvl WP? '}'
    ;

obj_cons_kvl
    : TAG WP exp
    | TAG WP exp WP obj_cons_kvl
    ;

