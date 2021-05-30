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


% If -> do
if_nonvar_do(V, T) :- (nonvar(V) -> call(T) ; true).
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
	expr_p(Expr), whitespace_p(C).

expr_p(variable(Name)) -->
	{ if_nonvar_do(Name, C = []) },
	whitespace_p(C),
	[Name],
	{ member(Name, ['x', 'y', 'z']) }.

expr_p(const(Value)) -->
	{ if_nonvar_do(Value, (C = [], number_chars(Value, Chars))) },
	whitespace_p(C),
	float_p(Chars),
	{ Chars = [_ | _], number_chars(Value, Chars) }.

expr_p(operation(Op, A)) -->
	{ if_nonvar_do(Op, (C1 = [], C2 = [], C3 = [])) }, 
	whitespace_p(C1), oper_p(Op), whitespace_p(C2), ['('], expr_p(A), whitespace_p(C3), [')'].

expr_p(operation(Op, A, B)) --> 
	{ if_nonvar_do(Op, (C1 = [], C2 = [' '], C3 = [' '], C4 = []), C3 = []) },
	whitespace_p(C1), ['('], expr_p(A), whitespace_p(C2), oper_p(Op), whitespace_p(C3), expr_p(B), whitespace_p(C4), [')'].

integer_p([]) --> [].
integer_p([H | T]) --> 
  { member(H, ['0', '1', '2', '3', '4', '5', '6', '7', '8', '9'])},
  [H], 
  integer_p(T).

float_p(Chars) --> 
	{ if_nonvar_do(Chars, append(Chars1, ['.' | Chars2], Chars)) },
	integer_p(Chars1),
	{ Chars1 = [_ | _] },
	['.'],
	integer_p(Chars2),
	{ Chars2 = [_ | _], append(Chars1, ['.' | Chars2], Chars) }.

float_p(Chars) --> 
	{ if_nonvar_do(Chars, append(['-' | Chars1], ['.' | Chars2], Chars)) },
	['-'],
	integer_p(Chars1),
	{ Chars1 = [_ | _] },
	['.'],
	integer_p(Chars2),
	{ Chars2 = [_ | _], append(['-' | Chars1], ['.' | Chars2], Chars) }.

whitespace_p([]) --> [].
whitespace_p([' ' | T]) -->
	[' '],
	whitespace_p(T).


% Infix parser
infix_str(Expr, Str) :- ground(Expr), !, phrase(parser(Expr), Chars), atom_chars(Str, Chars).
infix_str(Expr, Str) :-   atom(Str), !, atom_chars(Str, Chars), phrase(parser(Expr), Chars).
