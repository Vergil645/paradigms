% Init
init(MAX_N) :- N is sqrt(MAX_N), init_loop(N, 2).
init_loop(MAX_N, I) :- I > MAX_N, !.
init_loop(MAX_N, I) :- prime(I), assert(primes_table(I)), I1 is I + 1, !, init_loop(MAX_N, I1).
init_loop(MAX_N, I) :- I1 is I + 1, init_loop(MAX_N, I1).

% Prime
prime(N) :- \+ composite(N).

% Composite
composite(N) :- primes_table(X), X =< sqrt(N), 0 is N mod X, !.

% Divisors (N is number)
prime_divisors(N, Divisors) :- number(N), !, make_divisors(N, 2, Divisors).
make_divisors(1, _, []).
make_divisors(N, X, [N]) :- X =< N, prime(N), !.
make_divisors(N, X, [X | Tail]) :- 0 is mod(N, X), !, N1 is N / X, make_divisors(N1, X, Tail).
make_divisors(N, X, Divisors) :- X < N, X1 is X + 1, !, make_divisors(N, X1, Divisors).

% Divisors (Divisors is not variable)
prime_divisors(N, Divisors) :- nonvar(Divisors), !, make_number(N, 1, Divisors), prime_divisors(N, Divisors).
make_number(N, N, []).
make_number(N, R, [X | Tail]) :- R1 is R * X, make_number(N, R1, Tail).

% Power divisors (N is number)
power_divisors(N, I, D) :- number(N), !, make_power_divisors(N, I, 2, D).
make_power_divisors(1, _, _, []).
make_power_divisors(N, I, X, D) :- X =< N, prime(N), rep(N, I, D), !.
make_power_divisors(N, I, X, D) :- 0 is mod(N, X), N1 is N / X, rep(X, I, D1), !, make_power_divisors(N1, I, X, D2), append(D1, D2, D).
make_power_divisors(N, I, X, D) :- X < N, X1 is X + 1, make_power_divisors(N, I, X1, D).
rep(N, 0, []).
rep(N, I, [N | Tail]) :- I1 is I - 1, rep(N, I1, Tail).

% Power divisors (N is variable)
power_divisors(N, I, D) :- var(N), !, make_power_number(N, I, 1, D), power_divisors(N, I, D).
make_power_number(N, _, N, []).
make_power_number(N, I, R, [X | Tail]) :- R1 is R * X, skip([X | Tail], I, D), make_power_number(N, I, R1, D).
skip([], _, []).
skip(D, 0, D).
skip([X | Tail], I, D) :- I1 is I - 1, skip(Tail, I1, D).