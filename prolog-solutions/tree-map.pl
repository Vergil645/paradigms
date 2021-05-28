% Create node
create_node(K, V, node(K, V, 1, 0, null, null)).

create_node(K, V, L, R, Node) :-
	get_height(L, LH),
	get_height(R, RH),
	(LH > RH -> H is LH + 1 ; H is RH + 1),
	D is LH - RH,
	Node = node(K, V, H, D, L, R).

get_height(null, 0).
get_height(node(_, _, H, _, _, _), H).

get_diff(null, 0).
get_diff(node(_, _, _, D, _, _), D).


% Small rotation
small_left_rotation(node(K, V, _, _, L, node(RK, RV, _, _, RL, RR)), Node) :-
	create_node(K, V, L, RL, NewRL),
	create_node(RK, RV, NewRL, RR, Node).

small_right_rotation(node(K, V, _, _, node(LK, LV, _, _, LL, LR), R), Node) :-
	create_node(K, V, LR, R, NewLR),
	create_node(LK, LV, LL, NewLR, Node).


% Big rotation
big_left_rotation(node(K, V, _, _, L, R), Node) :-
	small_right_rotation(R, NewR),
	create_node(K, V, L, NewR, Tmp),
	small_left_rotation(Tmp, Node).

big_right_rotation(node(K, V, _, _, L, R), Node) :-
	small_left_rotation(L, NewL),
	create_node(K, V, NewL, R, Tmp),
	small_right_rotation(Tmp, Node).


% Balance
balance(node(K, V, H, -2, L, R), Node) :-
	get_diff(R, RD),
	RD =< 0,
	!, small_left_rotation(node(K, V, H, -2, L, R), Node).

balance(node(K, V, H, -2, L, R), Node) :-
	!, big_left_rotation(node(K, V, H, -2, L, R), Node).

balance(node(K, V, H, 2, L, R), Node) :-
	get_diff(L, LD),
	LD >= 0,
	!, small_right_rotation(node(K, V, H, 2, L, R), Node).

balance(node(K, V, H, 2, L, R), Node) :-
	!, big_right_rotation(node(K, V, H, 2, L, R), Node).

balance(Node, Node).


% Map build
map_build(ListMap, TreeMap) :- build_loop(ListMap, null, TreeMap).

build_loop([], TreeMap, TreeMap).

build_loop([(Key, Value) | Tail], T, TreeMap) :- 
	map_put(T, Key, Value, T1),
	build_loop(Tail, T1, TreeMap).


% Map get
map_get(node(Key, Value, _, _, _, _), Key, Value).

map_get(node(K, _, _, _, L, _), Key, Value) :-
	Key < K, 
	!, map_get(L, Key, Value).

map_get(node(_, _, _, _, _, R), Key, Value) :-
	map_get(R, Key, Value).


% Map put
map_put(null, Key, Value, Node) :- create_node(Key, Value, Node).

map_put(node(Key, _, H, D, L, R), Key, Value, node(Key, Value, H, D, L, R)) :- !.

map_put(node(K, V, _, _, L, R), Key, Value, Node) :-
	Key < K, 
	!, map_put(L, Key, Value, NewL), 
	create_node(K, V, NewL, R, Node1),
	balance(Node1, Node).

map_put(node(K, V, _, _, L, R), Key, Value, Node) :-
	map_put(R, Key, Value, NewR),
	create_node(K, V, L, NewR, Node1), 
	balance(Node1, Node).


% Map remove
map_remove(TreeMap, Key, TreeMap) :- not map_get(TreeMap, Key, _), !.

map_remove(TreeMap, Key, Result) :- remove(TreeMap, Key, Result).

remove(node(Key, _, _, _, null, null), Key, null) :- !.

remove(node(Key, _, _, D, L, R), Key, Node) :- 
	D >= 0, 
	!, map_getLast(L, (NewK, NewV)), 
	remove(L, NewK, NewL), 
	create_node(NewK, NewV, NewL, R, Node1), 
	balance(Node1, Node).

remove(node(Key, _, _, D, L, R), Key, Node) :- 
	!, map_getFirst(R, (NewK, NewV)), 
	remove(R, NewK, NewR), 
	create_node(NewK, NewV, L, NewR, Node1), 
	balance(Node1, Node).

remove(node(K, V, _, _, L, R), Key, Node) :- 
	Key < K, 
	!, remove(L, Key, NewL), 
	create_node(K, V, NewL, R, Node1),
	balance(Node1, Node).

remove(node(K, V, _, _, L, R), Key, Node) :-
	remove(R, Key, NewR),
	create_node(K, V, L, NewR, Node1), 
	balance(Node1, Node).


% Map get last
map_getLast(node(K, V, _, _, _, null), (K, V)) :- !.
map_getLast(node(_, _, _, _, _, R), (MaxK, MaxV)) :- map_getLast(R, (MaxK, MaxV)).


% Map remove last
map_removeLast(null, null).
map_removeLast(TreeMap, Result) :-
	map_getLast(TreeMap, (K, _)),
	map_remove(TreeMap, K, Result).


% Map get first
map_getFirst(node(K, V, _, _, null, _), (K, V)) :- !.
map_getFirst(node(_, _, _, _, L, _), (MinK, MinV)) :- map_getFirst(L, (MinK, MinV)).


% Map remove first
map_removeFirst(null, null).
map_removeFirst(TreeMap, Result) :-
	map_getFirst(TreeMap, (K, _)),
	map_remove(TreeMap, K, Result).


% Merge
merge(T1, null, T1) :- !.
merge(null, T2, T2) :- !.

merge(T1, T2, T) :-
	get_height(T1, H1), 
	get_height(T2, H2), 
	H1 =< H2, 
	!, map_get_max(T1, SK, SV),
	remove(T1, SK, NewT1),
	merge_to_right(NewT1, T2, SK, SV, T).

merge(T1, T2, T) :- 
	map_get_min(T2, SK, SV),
	remove(T2, SK, NewT2),
	merge_to_left(T1, NewT2, SK, SV, T).

merge_to_right(T1, T2, SK, SV, Node) :-
	get_height(T1, H1), 
	get_height(T2, H2), 
	H1 >= H2,
	!, create_node(SK, SV, T1, T2, Node1),
	balance(Node1, Node).

merge_to_right(T1, node(K2, V2, _, _, L, R), SK, SV, Node) :-
	merge_to_right(T1, L, SK, SV, NewL),
	create_node(K2, V2, NewL, R, Node1),
	balance(Node1, Node).

merge_to_left(T1, T2, SK, SV, Node) :-
	get_height(T1, H1), 
	get_height(T2, H2), 
	H1 =< H2,
	!, create_node(SK, SV, T1, T2, Node1), 
	balance(Node1, Node).

merge_to_left(node(K1, V1, _, _, L, R), T2, SK, SV, Node) :-
	merge_to_left(R, T2, SK, SV, NewR),
	create_node(K1, V1, L, NewR, Node1),
	balance(Node1, Node).


% Split
split(null, _, null, null).

split(node(K, V, _, _, L, R), X, Node1, Node2) :-
	K =< X, 
	!, split(R, X, T1, Node2), 
	special_merge(L, T1, K, V, Node1).

split(node(K, V, _, _, L, R), X, Node1, Node2) :-
	split(L, X, Node1, T2),
	special_merge(T2, R, K, V, Node2).

special_merge(T1, T2, SK, SV, Node) :- 
	get_height(T1, H1), 
	get_height(T2, H2), 
	H1 =< H2, 
	!, merge_to_right(T1, T2, SK, SV, Node).

special_merge(T1, T2, SK, SV, Node) :-
	merge_to_left(T1, T2, SK, SV, Node).
