% Init
init(MAX_N) :- N is sqrt(MAX_N), init_loop(N, 2).
init_loop(MAX_N, I) :- I > MAX_N, !.
init_loop(MAX_N, I) :- prime(I), !, assert(primes_table(I)), I1 is I + 1, init_loop(MAX_N, I1).
init_loop(MAX_N, I) :- I1 is I + 1, init_loop(MAX_N, I1).

% Prime
prime(N) :- \+ composite(N).

% Composite
composite(N) :- primes_table(X), X =< sqrt(N), 0 is N mod X, !.

% Divisors (N is number)
prime_divisors(N, Divisors) :- number(N), !, make_divisors(N, 2, Divisors).
make_divisors(1, _, []).
make_divisors(N, I, [N]) :- I =< N, prime(N), !.
make_divisors(N, I, [I | Tail]) :- 0 is mod(N, I), !, N1 is N / I, make_divisors(N1, I, Tail).
make_divisors(N, I, Divisors) :- I < N, I1 is I + 1, make_divisors(N, I1, Divisors).

% Divisors (Divisors is not variable)
prime_divisors(N, Divisors) :- nonvar(Divisors), !, make_number(N, 1, Divisors), prime_divisors(N, Divisors).
make_number(N, N, []).
make_number(N, R, [X | Tail]) :- !, R1 is R * X, make_number(N, R1, Tail).
