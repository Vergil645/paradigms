% Secondary methods
sorted([]).
sorted([(_, _)]).
sorted([(K1, _), (K2, _) | Tail]) :- K1 =< K2.


% Node methods
get_max(leaf(K, _), K).
get_max(node2(Max, _, _, _), Max).
get_max(node3(Max, _, _, _, _, _), Max).

create_node(Son1, null, null, Son1).

create_node(Son1, Son2, null, node2(Max, Key1, Son1, Son2)) :-
	get_max(Son2, Max),
	get_max(Son1, Key1).

create_node(Son1, Son2, Son3, node3(Max, Key1, Key2, Son1, Son2, Son3)) :-
	get_max(Son3, Max),
	get_max(Son1, Key1),
	get_max(Son2, Key2).

create_node(Son1, Son2, Son3, null, Node1, null) :- 
	!, create_node(Son1, Son2, Son3, Node1).

create_node(Son1, Son2, null, Son4, Node1, null) :- 
	!, create_node(Son1, Son2, Son4, Node1).

create_node(Son1, null, Son3, Son4, Node1, null) :- 
	!, create_node(Son1, Son3, Son4, Node1).

create_node(Son1, Son2, Son3, Son4, Node1, Node2) :-
	create_node(Son1, Son2, null, Node1),
	create_node(Son3, Son4, null, Node2).


% TreeMap constructors
map_build([], null) :- !.

map_build(ListMap, TreeMap) :- 
	sorted(ListMap), 
	!, build_leafs(ListMap, LeafsList), 
	build_tree(LeafsList, TreeMap).

build_leafs([], []).

build_leafs([(Key, Value) | T1], [leaf(Key, Value) | T2]) :- 
	build_leafs(T1, T2).

build_tree([Node], Node).

build_tree(NodesList, TreeMap) :- 
	build_nodes(NodesList, NextNodesList), 
	build_tree(NextNodesList, TreeMap).

build_nodes([], []).
build_nodes([Son1, Son2, Son3], [Node]) :- 
	!, create_node(Son1, Son2, Son3, Node).
	
build_nodes([Son1, Son2 | T1], [Node | T2]) :- 
	create_node(Son1, Son2, null, Node), 
	build_nodes(T1, T2).


% Map get
map_get(leaf(K, V), Key, Value) :- 
	Key == K, 
	Value = V.

map_get(node2(_, Key1, _, Son2), Key, Value) :- 
	Key1 < Key, !, 
	map_get(Son2, Key, Value).
	
map_get(node2(_, _, Son1, _), Key, Value) :- 
	map_get(Son1, Key, Value).

map_get(node3(_, _, Key2, _, _, Son3), Key, Value) :- 
	Key2 < Key, !, 
	map_get(Son3, Key, Value).
	
map_get(node3(_, Key1, _, _, Son2, _), Key, Value) :- 
	Key1 < Key, !, 
	map_get(Son2, Key, Value).
	
map_get(node3(_, _, _, Son1, _, _), Key, Value) :- 
	map_get(Son1, Key, Value).


% Map put
map_put(TreeMap, Key, Value, Result) :- 
	map_get(TreeMap, Key, _), 
	!, tree_set(TreeMap, Key, Value, Result).

map_put([], Key, Value, leaf(Key, Value)) :- !.

map_put(TreeMap, Key, Value, Result) :- 
	tree_add(TreeMap, Key, Value, NewTree1, NewTree2), 
	create_node(NewTree1, NewTree2, null, Result).

tree_set(leaf(Key, V), Key, Value, leaf(Key, Value)).

tree_set(node2(Max, Key1, Son1, Son2), Key, Value, NewNode) :- 
	Key1 < Key, !, 
	tree_set(Son2, Key, Value, NewSon2), 
	create_node(Son1, NewSon2, null, NewNode).
	
tree_set(node2(Max, Key1, Son1, Son2), Key, Value, NewNode) :- 
	tree_set(Son1, Key, Value, NewSon1), 
	create_node(NewSon1, Son2, null, NewNode).

tree_set(node3(Max, Key1, Key2, Son1, Son2, Son3), Key, Value, NewNode) :- 
	Key2 < Key, !, 
	tree_set(Son3, Key, Value, NewSon3), 
	create_node(Son1, Son2, NewSon3, NewNode).
	
tree_set(node3(Max, Key1, Key2, Son1, Son2, Son3), Key, Value, NewNode) :- 
	Key1 < Key, !, 
	tree_set(Son2, Key, Value, NewSon2), 
	create_node(Son1, NewSon2, Son3, NewNode).
	
tree_set(node3(Max, Key1, Key2, Son1, Son2, Son3), Key, Value, NewNode) :- 
	tree_set(Son1, Key, Value, NewSon1), 
	create_node(NewSon1, Son2, Son3, NewNode).

tree_add(null, Key, Value, leaf(Key, Value), null).

tree_add(leaf(K, V), Key, Value, leaf(Key, Value), leaf(K, V)) :- Key < K, !.
tree_add(leaf(K, V), Key, Value, leaf(K, V), leaf(Key, Value)).

tree_add(node2(Max, Key1, Son1, Son2), Key, Value, NewNode, null) :-
	Key1 < Key, !, 
	tree_add(Son2, Key, Value, NewSon2, NewSon3),
	create_node(Son1, NewSon2, NewSon3, NewNode).

tree_add(node2(Max, Key1, Son1, Son2), Key, Value, NewNode, null) :-
	tree_add(Son1, Key, Value, NewSon1, NewSon2),
	create_node(NewSon1, NewSon2, Son2, NewNode).

tree_add(node3(Max, Key1, Key2, Son1, Son2, Son3), Key, Value, NewNode1, NewNode2) :- 
	Key2 < Key, 
	tree_add(Son3, Key, Value, NewSon3, NewSon4), 
	!, create_node(Son1, Son2, NewSon3, NewSon4, NewNode1, NewNode2).

tree_add(node3(Max, Key1, Key2, Son1, Son2, Son3), Key, Value, NewNode1, NewNode2) :- 
	Key1 < Key, 
	tree_add(Son2, Key, Value, NewSon2, NewSon3), 
	!, create_node(Son1, NewSon2, NewSon3, Son3, NewNode1, NewNode2).

tree_add(node3(Max, Key1, Key2, Son1, Son2, Son3), Key, Value, NewNode1, NewNode2) :- 
	tree_add(Son1, Key, Value, NewSon1, NewSon2), 
	create_node(NewSon1, NewSon2, Son2, Son3, NewNode1, NewNode2).


% Map build (unsorted)
map_build(ListMap, TreeMap) :- 
	not sorted(ListMap), 
	!, build_loop(ListMap, null, TreeMap).

build_loop([], TreeMap, TreeMap).

build_loop([(Key, Value) | Tail], Tree, TreeMap) :- 
	map_put(Tree, Key, Value, Tree1),
	build_loop(Tail, Tree1, TreeMap).


% Map remove
map_remove(TreeMap, Key, TreeMap) :- not map_get(TreeMap, Key, _), !.
map_remove(TreeMap, Key, Result) :- 
	.

