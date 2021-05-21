% Init
init(MAX_N) :- N is sqrt(MAX_N), init_loop(N, 2).
init_loop(MAX_N, I) :- I > MAX_N, !.
init_loop(MAX_N, I) :- prime(I), !, assert(primes_table(I)), I1 is I + 1, init_loop(MAX_N, I1).
init_loop(MAX_N, I) :- I1 is I + 1, init_loop(MAX_N, I1).

% Prime
prime(N) :- \+ composite(N).

% Composite
composite(N) :- primes_table(X), 0 is N mod X, X * X =< N.

% Divisors
prime_divisors(N, Divisors) :- number(N), !, make_divisors(N, 2, Divisors).
prime_divisors(N, Divisors) :- ground(Divisors), !, make_number(N, 1, Divisors).

make_divisors(1, _, []).
make_divisors(N, X, [N]) :- X =< N, prime(N), !.
make_divisors(N, X, [X | Tail]) :- 0 is mod(N, X), !, N1 is N / X, make_divisors(N1, X, Tail).
make_divisors(N, X, Divisors) :- X < N, X1 is X + 1, !, make_divisors(N, X1, Divisors).

make_number(N, N, []).
make_number(N, R, [X]) :- prime(X), N is R * X.
make_number(N, R, [X, Y | Tail]) :- X =< Y, prime(X), R1 is R * X, make_number(N, R1, [Y | Tail]).

% Prime index
prime_index(P, N) :- index_loop(P, N, 2, 0).

index_loop(P, N, X, C) :- N == C, !, P is X - 1.
index_loop(P, N, X, C) :- P == X, !, prime(P), N is C + 1.
index_loop(P, N, X, C) :- prime(X), !, X1 is X + 1, C1 is C + 1, index_loop(P, N, X1, C1).
index_loop(P, N, X, C) :- X1 is X + 1, index_loop(P, N, X1, C).
