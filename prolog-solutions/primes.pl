% Init
init(MAX_N) :- N is sqrt(MAX_N), init_loop(N, 2).
init_loop(MAX_N, I) :- I > MAX_N, !.
init_loop(MAX_N, I) :- prime(I), !, assert(primes_table(I)), I1 is I + 1, init_loop(MAX_N, I1).
init_loop(MAX_N, I) :- I1 is I + 1, init_loop(MAX_N, I1).

% Prime
prime(N) :- \+ composite(N).

% Composite
% :NOTE: X =< sqrt(N),
composite(N) :- primes_table(X), X =< sqrt(N), 0 is N mod X.

% Divisors (N is number)
prime_divisors(N, Divisors) :- number(N), !, make_divisors(N, 2, Divisors).
% :NOTE: Лишняя работа
prime_divisors(N, Divisors) :- ground(Divisors), !, make_number(N, 1, Divisors), prime_divisors(N, Divisors).

make_divisors(1, _, []).
make_divisors(N, X, [N]) :- X =< N, prime(N), !.
make_divisors(N, X, [X | Tail]) :- 0 is mod(N, X), !, N1 is N / X, make_divisors(N1, X, Tail).
make_divisors(N, X, Divisors) :- X < N, X1 is X + 1, !, make_divisors(N, X1, Divisors).

% Divisors (Divisors is not variable)
make_number(N, N, []).
make_number(N, R, [X | Tail]) :- R1 is R * X, make_number(N, R1, Tail).

% Prime index (P - number)
% :NOTE: Упростить
prime_index(P, N) :- number(P), !, prime(P), loop_1(P, 2, N).
loop_1(P, P, 1) :- !.
loop_1(P, X, N) :- prime(X), !, X1 is X + 1, loop_1(P, X1, N1), N is N1 + 1.
loop_1(P, X, N) :- X1 is X + 1, loop_1(P, X1, N), !.

% Prime index (N - number)
prime_index(P, N) :- number(N), !, loop_2(N, 2, P).
loop_2(0, X, P) :- P is X - 1, !.
loop_2(N, X, P) :- prime(X), !, X1 is X + 1, N1 is N - 1, loop_2(N1, X1, P).
loop_2(N, X, P) :- X1 is X + 1, loop_2(N, X1, P), !.
