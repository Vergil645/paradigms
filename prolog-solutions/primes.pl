% Init
init(MAX_N) :- assert(max_n(MAX_N)), MAX_N1 is floor(sqrt(MAX_N)) + 1, loop_1(MAX_N1, 2, 0), !.

loop_1(MAX_N, I, IP) :- first_divisor(I, PI), !, loop_2(MAX_N, I, PI, 0), I1 is I + 1, loop_1(MAX_N, I1, IP).
loop_1(MAX_N, I, IP) :- I =< MAX_N, !, assert(first_divisor(I, I)), assert(primes_table(IP, I)), IP1 is IP + 1, loop_2(MAX_N, I, I, 0), I1 is I + 1, loop_1(MAX_N, I1, IP1).
loop_1(MAX_N, I, IP) :- true, !.

loop_2(MAX_N, I, PI, J) :- primes_table(J, PJ), PJ =< PI, X is I * PJ, X =< MAX_N, !, assert(first_divisor(X, PJ)), J1 is J + 1, loop_2(MAX_N, I, PI, J1).
loop_2(MAX_N, I, PI, J) :- true, !.

% Prime
prime(2) :- !.
prime(A) :- 1 is mod(A, 2), S is sqrt(A), findall(X, (first_divisor(X, X), X =< S, 0 is A mod X), []), !.


% Composite
composite(N) :- \+ prime(N).


% Divisors
prime_divisors(N, Divisors) :- number(N), !, make_divisors(N, 2, Divisors).
% N, X - numbers
%make_divisors(1, []) :- !.
%make_divisors(N, [X | Tail]) :- !, first_divisor(N, X), N1 is N / X, make_divisors(N1, Tail).
make_divisors(1, _, []) :- !.
make_divisors(N, I, [N]) :- I =< N, prime(N), !.
make_divisors(N, I, [I | Tail]) :- 0 is mod(N, I), !, N1 is N / I, make_divisors(N1, I, Tail).
make_divisors(N, I, Divisors) :- I < N, !, I1 is I + 1, make_divisors(N, I1, Divisors).

prime_divisors(N, Divisors) :- nonvar(Divisors), !, make_number(N, 1, Divisors), prime_divisors(N, Divisors).
make_number(N, R, []) :- !, N is R.
make_number(N, R, [X | Tail]) :- max_n(Max), !, R1 is R * X, R1 =< Max, make_number(N, R1, Tail).

%prime_divisors(N, Divisors) :- make_number(N, 1, Divisors).
%% R, X, Y - numbers
%make_number(N, R, []) :- !, N is R.
%make_number(N, R, [X]) :- !, prime(X), N is R * X.
%make_number(N, R, [X | [Y | Tail]]) :- !, X =< Y, prime(X), R1 is R * X, make_number(N, R1, [Y | Tail]).
