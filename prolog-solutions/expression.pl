% Import
:- load_library('alice.tuprolog.lib.DCGLibrary').


% Lookup
lookup(K, [(K, V) | _], V).
lookup(K, [_ | T], V) :- lookup(K, T, V).


% Unary operations
operation(op_negate, X, R) :- R is -X.


% Binary operations
operation(op_add,      X, Y, R) :- R is X + Y.
operation(op_subtract, X, Y, R) :- R is X - Y.
operation(op_multiply, X, Y, R) :- R is X * Y.
operation(op_divide,   X, Y, R) :- R is X / Y.


% Evaluate
evaluate(const(Value), _, Value).

evaluate(variable(Name), VarMap, R) :- lookup(Name, VarMap, R).

evaluate(operation(Op, A), VarMap, R) :-
	evaluate(A, VarMap, RA),
	operation(Op, RA, R).

evaluate(operation(Op, A, B), VarMap, R) :- 
	evaluate(A, VarMap, RA),
	evaluate(B, VarMap, RB),
	operation(Op, RA, RB, R).


% If nonvar(V) then do(T)
if_nonvar_do(V, T) :- (nonvar(V) -> call(T) ; true).

% If nonvar(V) then do(T) else do(G)
if_nonvar_do(V, T, G) :- (nonvar(V) -> call(T) ; call(G)).


% Operation parser
oper_p(op_negate)   --> ['n', 'e', 'g', 'a', 't', 'e'].
oper_p(op_add)      --> ['+'].
oper_p(op_subtract) --> ['-'].
oper_p(op_multiply) --> ['*'].
oper_p(op_divide)   --> ['/'].


% Expression parser
parser(Expr) --> 
	{ if_nonvar_do(Expr, C = []) },
	expr_p(Expr), ws_p(C).

expr_p(variable(Name)) -->
	{ if_nonvar_do(Name, C = [], member(Name, ['x', 'y', 'z'])) },
	ws_p(C), [Name].

expr_p(const(Value)) -->
	{ if_nonvar_do(Value, (C = [], number_chars(Value, Chars))) },
	ws_p(C), float_p(Chars),
	{ number_chars(Value, Chars) }.

expr_p(operation(Op, A)) -->
	{ if_nonvar_do(Op, (C1 = [], C2 = [], C3 = [])) }, 
	ws_p(C1), oper_p(Op), ws_p(C2), ['('], expr_p(A), ws_p(C3), [')'].

expr_p(operation(Op, A, B)) --> 
	{ if_nonvar_do(Op, (C1 = [], C2 = [' '], C3 = [' '], C4 = []), C3 = []) },
	ws_p(C1), ['('], expr_p(A), ws_p(C2), oper_p(Op), ws_p(C3), expr_p(B), ws_p(C4), [')'].

uint_p([H]) --> 
	{ member(H, ['0', '1', '2', '3', '4', '5', '6', '7', '8', '9'])}, 
	[H].
uint_p([H | T]) --> 
	{ member(H, ['0', '1', '2', '3', '4', '5', '6', '7', '8', '9'])},
  [H], uint_p(T).

int_p(['-' | T]) --> ['-'], uint_p(T).
int_p(Chars) --> uint_p(Chars).

float_p(Chars) --> 
	{ if_nonvar_do(Chars, append(Chars1, ['.' | Chars2], Chars)) },
	int_p(Chars1), ['.'], uint_p(Chars2),
	{ append(Chars1, ['.' | Chars2], Chars) }.

ws_p([]) --> [].
ws_p([' ' | T]) --> [' '], ws_p(T).


% Infix parser
infix_str(Expr, Str) :- ground(Expr), !, phrase(parser(Expr), Chars), atom_chars(Str, Chars).
infix_str(Expr, Str) :-    atom(Str), !, atom_chars(Str, Chars), phrase(parser(Expr), Chars).
