% Create node
create_node(K, V, Node) :-
	rand_int(1000000007, P),
	Node = node(K, V, P, null, null).


% Split
split(null, _, null, null).

split(node(K, V, P, L, R), X, Node1, Node2) :-
	K =< X,
	!, split(R, X, NewR, Node2),
	Node1 = node(K, V, P, L, NewR).
	
split(node(K, V, P, L, R), X, Node1, Node2) :-
	split(L, X, Node1, NewL),
	Node2 = node(K, V, P, NewL, R).


% Merge
merge(T1, null, T1) :- !.
merge(null, T2, T2).

merge(node(K1, V1, P1, L1, R1), node(K2, V2, P2, L2, R2), Node) :-
	P1 > P2, 
	!, merge(R1, node(K2, V2, P2, L2, R2), NewR1),
	Node = node(K1, V1, P1, L1, NewR1).

merge(node(K1, V1, P1, L1, R1), node(K2, V2, P2, L2, R2), Node) :-
	merge(node(K1, V1, P1, L1, R1), L2, NewL2),
	Node = node(K2, V2, P2, NewL2, R2).


% Map get
map_get(node(Key, Value, _, _, _), Key, Value).

map_get(node(K, V, _, L, _), Key, Value) :-
	Key < K, 
	!, map_get(L, Key, Value).

map_get(node(K, V, _, _, R), Key, Value) :-
	map_get(R, Key, Value).


% Map put
map_put(TreeMap, Key, Value, Result) :- 
	map_get(TreeMap, Key, _), 
	!, set_node(TreeMap, Key, Value, Result).

map_put(TreeMap, Key, Value, Result) :-
	create_node(Key, Value, Node), 
	split(TreeMap, Key, T1, T2), 
	merge(T1, Node, Tmp),
	merge(Tmp, T2, Result).

set_node(node(Key, _, P, L, R), Key, Value, node(Key, Value, P, L, R)).

set_node(node(K, V, P, L, R), Key, Value, Node) :-
	Key < K, 
	!, set_node(L, Key, Value, NewL), 
	Node = node(K, V, P, NewL, R).

set_node(node(K, V, P, L, R), Key, Value, Node) :-
	set_node(R, Key, Value, NewR), 
	Node = node(K, V, P, L, NewR).


% Map build
map_build(ListMap, TreeMap) :- build_loop(ListMap, null, TreeMap).

build_loop([], TreeMap, TreeMap).

build_loop([(Key, Value) | Tail], T, TreeMap) :- 
	map_put(T, Key, Value, T1),
	build_loop(Tail, T1, TreeMap).


% Map remove
map_remove(TreeMap, Key, TreeMap) :- not map_get(TreeMap, Key, _), !.

map_remove(TreeMap, Key, Result) :-
	split(TreeMap, Key, Tmp, T2),
	Key1 is Key - 1,
	split(Tmp, Key1, T1, _),
	merge(T1, T2, Result).
