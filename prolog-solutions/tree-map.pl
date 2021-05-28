% Create node
create_node(K, V, node(K, V, 1, 0, 1, null, null)).

create_node(K, V, L, R, Result) :-
	get_height(L, LH),
	get_height(R, RH),
	(LH > RH -> H is LH + 1 ; H is RH + 1),
	D is LH - RH,
	get_size(L, LS),
	get_size(R, RS),
	S is LS + RS + 1,
	Result = node(K, V, H, D, S, L, R).

%get_key(null, _) :- !, fail.
get_key(node(K, _, _, _, _, _, _), K).

%get_value(null, _) :- !, fail.
get_value(node(_, V, _, _, _, _, _), V).

get_height(null, 0).
get_height(node(_, _, H, _, _, _, _), H).

get_diff(null, 0).
get_diff(node(_, _, _, D, _, _, _), D).

get_size(null, 0).
get_size(node(_, _, _, _, S, _, _), S).

%get_left(null, _) :- !, fail.
get_left(node(_, _, _, _, _, L, _), L).

%get_right(null, _) :- !, fail.
get_right(node(_, _, _, _, _, _, R), R).


% Small rotation
small_left_rotation(Node, Result) :-
	get_key(Node, K), get_value(Node, V), get_left(Node, L), get_right(Node, R),
	get_key(R, RK), get_value(R, RV), get_left(R, RL), get_right(R, RR),
	create_node(K, V, L, RL, NewRL),
	create_node(RK, RV, NewRL, RR, Result).

small_right_rotation(Node, Result) :-
	get_key(Node, K), get_value(Node, V), get_left(Node, L), get_right(Node, R),
	get_key(L, LK), get_value(L, LV), get_left(L, LL), get_right(L, LR),
	create_node(K, V, LR, R, NewLR),
	create_node(LK, LV, LL, NewLR, Result).


% Big rotation
big_left_rotation(Node, Result) :-
	get_key(Node, K), get_value(Node, V), get_left(Node, L), get_right(Node, R),
	small_right_rotation(R, NewR),
	create_node(K, V, L, NewR, Tmp),
	small_left_rotation(Tmp, Result).

big_right_rotation(Node, Result) :-
	get_key(Node, K), get_value(Node, V), get_left(Node, L), get_right(Node, R),
	small_left_rotation(L, NewL),
	create_node(K, V, NewL, R, Tmp),
	small_right_rotation(Tmp, Result).


% Balance
balance(Node, Result) :-
	get_right(Node, R),
	get_diff(Node, -2), get_diff(R, RD),
	RD =< 0,
	!, small_left_rotation(Node, Result).

balance(Node, Result) :-
	get_right(Node, R),
	get_diff(Node, -2), get_diff(R, RD),
	!, big_left_rotation(Node, Result).

balance(Node, Result) :-
	get_left(Node, L),
	get_diff(Node, 2), get_diff(L, LD),
	LD >= 0,
	!, small_right_rotation(Node, Result).

balance(Node, Result) :-
	get_diff(Node, 2),
	!, big_right_rotation(Node, Result).

balance(Node, Node).


% Map build
map_build(ListMap, TreeMap) :- build_loop(ListMap, null, TreeMap).

build_loop([], TreeMap, TreeMap).

build_loop([(Key, Value) | Tail], T, TreeMap) :- 
	map_put(T, Key, Value, T1),
	build_loop(Tail, T1, TreeMap).


% Map get
map_get(Node, Key, Value) :- get_key(Node, Key), !, get_value(Node, Value).

map_get(Node, Key, Value) :-
	get_key(Node, K),
	Key < K, 
	!, get_left(Node, L),
	map_get(L, Key, Value).

map_get(Node, Key, Value) :-
	get_right(Node, R),
	map_get(R, Key, Value).


% Map put
map_put(null, Key, Value, Result) :- !, create_node(Key, Value, Result).

map_put(Node, Key, Value, Result) :- 
	get_key(Node, Key),
	!, get_left(Node, L), get_right(Node, R),
	create_node(Key, Value, L, R, Result).

map_put(Node, Key, Value, Result) :-
	get_key(Node, K),
	Key < K, 
	!, get_value(Node, V), get_left(Node, L), get_right(Node, R),
	map_put(L, Key, Value, NewL), 
	create_node(K, V, NewL, R, Node1),
	balance(Node1, Result).

map_put(Node, Key, Value, Result) :-
	get_key(Node, K), get_value(Node, V), get_left(Node, L), get_right(Node, R),
	map_put(R, Key, Value, NewR),
	create_node(K, V, L, NewR, Node1), 
	balance(Node1, Result).


% Map remove
map_remove(TreeMap, Key, TreeMap) :- not map_get(TreeMap, Key, _), !.

map_remove(TreeMap, Key, Result) :- remove(TreeMap, Key, Result).

remove(Node, _, null) :- 
	get_left(Node, null), get_right(Node, null),
	!.

remove(Node, Key, Result) :- 
	get_key(Node, Key), 
	get_diff(Node, D),
	D >= 0, 
	!, get_left(Node, L), get_right(Node, R),
	map_getLast(L, (NewK, NewV)), 
	remove(L, NewK, NewL), 
	create_node(NewK, NewV, NewL, R, Node1), 
	balance(Node1, Result).

remove(Node, Key, Result) :- 
	get_key(Node, Key), 
	!, get_left(Node, L), get_right(Node, R),
	map_getFirst(R, (NewK, NewV)), 
	remove(R, NewK, NewR), 
	create_node(NewK, NewV, L, NewR, Node1), 
	balance(Node1, Result).

remove(Node, Key, Result) :- 
	get_key(Node, K),
	Key < K, 
	!, get_value(Node, V), get_left(Node, L), get_right(Node, R), 
	remove(L, Key, NewL), 
	create_node(K, V, NewL, R, Node1),
	balance(Node1, Result).

remove(Node, Key, Result) :-
	get_key(Node, K), get_value(Node, V), get_left(Node, L), get_right(Node, R), 
	remove(R, Key, NewR),
	create_node(K, V, L, NewR, Node1), 
	balance(Node1, Result).


% Map get last
map_getLast(Node, (K, V)) :- 
	get_right(Node, null), 
	!, get_key(Node, K), get_value(Node, V).
	
map_getLast(Node, (MaxK, MaxV)) :- 
	get_right(Node, R),
	map_getLast(R, (MaxK, MaxV)).


% Map remove last
map_removeLast(null, null).
map_removeLast(TreeMap, Result) :-
	map_getLast(TreeMap, (K, _)),
	map_remove(TreeMap, K, Result).


% Map get first
map_getFirst(Node, (K, V)) :- 
	get_left(Node, null), 
	!, get_key(Node, K), get_value(Node, V).
	
map_getFirst(Node, (MinK, MinV)) :- 
	get_left(Node, L),
	map_getFirst(L, (MinK, MinV)).


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

merge_to_right(T1, T2, SK, SV, Result) :-
	get_height(T1, H1), 
	get_height(T2, H2), 
	H1 >= H2,
	!, create_node(SK, SV, T1, T2, Tmp),
	balance(Tmp, Result).

merge_to_right(T1, Node2, SK, SV, Result) :-
	get_key(Node2, K2), get_value(Node2, V2), get_left(Node2, L2), get_right(Node2, R2),
	merge_to_right(T1, L2, SK, SV, NewL2),
	create_node(K2, V2, NewL2, R2, Tmp),
	balance(Tmp, Result).

merge_to_left(T1, T2, SK, SV, Result) :-
	get_height(T1, H1), 
	get_height(T2, H2), 
	H1 =< H2,
	!, create_node(SK, SV, T1, T2, Tmp), 
	balance(Tmp, Result).

merge_to_left(Node1, T2, SK, SV, Result) :-
	get_key(Node1, K1), get_value(Node1, V1), get_left(Node1, L1), get_right(Node1, R1),
	merge_to_left(R1, T2, SK, SV, NewR1),
	create_node(K1, V1, L1, NewR1, Tmp),
	balance(Tmp, Result).


% Split
split(null, _, null, null).

split(Node, X, Result1, Result2) :-
	get_key(Node, K), 
	K =< X, 
	!, get_value(Node, V), get_left(Node, L), get_right(Node, R), 
	split(R, X, T1, Result2), 
	special_merge(L, T1, K, V, Result1).

split(Node, X, Result1, Result2) :-
	get_key(Node, K), get_value(Node, V), get_left(Node, L), get_right(Node, R), 
	split(L, X, Result1, T2),
	special_merge(T2, R, K, V, Result2).

special_merge(T1, T2, SK, SV, Result) :- 
	get_height(T1, H1), 
	get_height(T2, H2), 
	H1 =< H2, 
	!, merge_to_right(T1, T2, SK, SV, Result).

special_merge(T1, T2, SK, SV, Result) :-
	merge_to_left(T1, T2, SK, SV, Result).


% Map headMapSize
map_headMapSize(Map, ToKey, Size) :-
	X is ToKey - 1,
	split(Map, X, T1, _), 
	get_size(T1, Size).


% Map tailMapSize
map_tailMapSize(Map, FromKey, Size) :-
	X is FromKey - 1,
	split(Map, X, _, T2),
	get_size(T2, Size).
