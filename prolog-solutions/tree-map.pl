% Node methods
set_parent(node0(_ , K, V), 
					 Parent, 
					 node0(Parent, K, V)).
set_parent(node2(_ , Max, Key1, Son1, Son2), 
					 Parent, 
					 node2(Parent, Max, Key1, Son1, Son2)).
set_parent(node3(_ , Max, Key1, Key2, Son1, Son2, Son3), 
					 Parent, 
					 node3(Parent, Max, Key1, Key2, Son1, Son2, Son3)).

get_max(leaf(K, _), K).
get_max(node2(Max, _, _, _), Max).
get_max(node3(Max, _, _, _, _, _), Max).


% TreeMap constructors
map_build([], null) :- !.
map_build(ListMap, TreeMap) :- build_leafs(ListMap, LeafsList), build_tree(LeafsList, TreeMap).

build_leafs([], []).
build_leafs([(Key, Value) | T1], [leaf(Key, Value) | T2]) :- build_leafs(T1, T2).

build_tree([Node], Node) :- !.
build_tree(NodesList, TreeMap) :- build_nodes(NodesList, NextNodesList), build_tree(NextNodesList, TreeMap).

build_nodes([], []).
build_nodes([Son1, Son2, Son3], [node3(Max, Key1, Key2, Son1, Son2, Son3)]) :- !,
		get_max(Son3, Max),
		get_max(Son1, Key1),
		get_max(Son2, Key2).
build_nodes([Son1, Son2 | T1], [node2(Max, Key1, Son1, Son2) | T2]) :- !,
		get_max(Son2, Max),
		get_max(Son1, Key1),
		build_nodes(T1, T2).


% Map get
map_get(leaf(K, V), Key, Value) :- Key == K, Value = V, !.

map_get(node2(_, Key1, _, Son2), Key, Value) :- Key1 < Key, !, map_get(Son2, Key, Value).
map_get(node2(_, _, Son1, _), Key, Value) :- map_get(Son1, Key, Value), !.

map_get(node3(_, _, Key2, _, _, Son3), Key, Value) :- Key2 < Key, !, map_get(Son3, Key, Value).
map_get(node3(_, Key1, _, _, Son2, _), Key, Value) :- Key1 < Key, !, map_get(Son2, Key, Value).
map_get(node3(_, _, _, Son1, _, _), Key, Value) :- map_get(Son1, Key, Value), !.


% Map put
map_put(null, Key, Value, leaf(Key, Value)). 
map_put(TreeMap, Key, Value, R) :- !.




%% Splay (Top-down)
%rotate_left(node(K, V, Left, node(RightK, RightV, RightLeft, RightRight), 
%						node(RightK, RightV, node(K, V, Left, RightLeft), RightRight))).
%						
%rotate_right(node(K, V, node(LeftK, LeftV, LeftLeft, LeftRight), Right),
%						 node(LeftK, LeftV, LeftLeft, node(K, V, LeftRight, Right))).
%
%break_left(node(K, V, Left, Right), null, null, 
%					 Right, node(K, V, Left, Null), node(K, V, Left, Null)).
%		
%break_left(node(K, V, Left, Right), RootL, node(LK, LV, LLeft, node(K, V, Left, Null)), 
%					 Right, RootL, node(K, V, Left, Null)).
%
%break_right(node(K, V, Left, Right), null, null, 
%					 Left, node(K, V, Null, Right), node(K, V, Null, Right)).
%		
%break_right(node(K, V, Left, Right), RootR, node(RK, RV, node(K, V, Null, Right), RRight), 
%					 Left, RootR, node(K, V, Null, Right)).
%
%assemble(node(K, V, Left, Right), 
%				 RootL, node(LK, LV, LLeft, Left), 
%				 RootR, node(RK, RV, Right, RRight),
%				 node(K, V, RootL, RootR)).
%
%assemble(node(K, V, Left, Right), 
%				 null, null, 
%				 RootR, node(RK, RV, Right, RRight),
%				 node(K, V, Left, RootR)).
%
%assemble(node(K, V, Left, Right), 
%				 RootL, node(LK, LV, LLeft, Left), 
%				 null, null,
%				 node(K, V, RootL, Right)).
%
%assemble(node(K, V, Left, Right),
%				 null, null,
%				 null, null,
%				 node(K, V, Left, Right)).
%
%splay(Key, T, NewT) :- 
%		splay_loop(Key, T, null, null, null, null, Tmp, RootL, L, RootR, R), 
%		assemble(Tmp, RootL, L, RootR, R, NewT).
%
%splay_loop(Key, 
%					 node(Key, V, Left, Right), RootL, L, RootR, R, 
%					 node(Key, V, Left, Right), RootL, L, RootR, R) :- !.
%
%get_key(node(K, _, _, _), K) :- !.
%
%splay_loop(Key, 
%					 node(K, V, Left, Right), RootL, L, RootR, R,
%					 Node, RootL, L, RootR1, R1) :-
%		Key < K, get_key(Left, LeftK), Key < LeftK, !, 
%		rotate_right(node(K, V, Left, Right), T),
%		break_right(T, RootR, R, Node, RootR1, R1).
%
%splay_loop(Key, 
%					 node(K, V, Left, Right), RootL, L, RootR, R,
%					 Node, RootL, L, RootR1, R1) :-
%		Key < K, !, 
%		break_right(node(K, V, Left, Right), RootR, R, Node, RootR1, R1).
%
%splay_loop(Key, 
%					 node(K, V, Left, Right), RootL, L, RootR, R,
%					 Node, RootL1, L1, RootR, R) :-
%		get_key(Right, RightK), Key > RightK, !, 
%		rotate_left(node(K, V, Left, Right), T),
%		break_left(T, RootL, L, Node, RootL1, L1).
%
%splay_loop(Key, 
%					 node(K, V, Left, Right), RootL, L, RootR, R,
%					 Node, RootL1, L1, RootR, R) :-
%		break_left(node(K, V, Left, Right), RootL, L, Node, RootL1, L1).
