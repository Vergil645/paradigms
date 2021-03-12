package queue;

// Model:
// [q_1, q_2, ..., q_n]
// n -- capacity of queue

// Inv:
// n >= 0 && forall i = 1, ..., n : q[i] != null

// Immutability:
// n == n' && forall i = 1, ..., n' : q[i] == q'[i]

public interface Queue {
    // Pred: x != null
    // Post: n == n' + 1 && q[n] == x && forall i = 1, ..., n' : q[i] == q'[i]
    void enqueue(Object x);

    // Pred: n > 0
    // Post: R == q'[1] && Immutability
    Object element();

    // Pred: n > 0
    // Post: R == q'[1] && n == n' - 1 && forall i = 2, ..., n' : q[i - 1] == q'[i]
    Object dequeue();

    // Pred: true
    // Post: R == n' && Immutability
    int size();

    // Pred: true
    // Post: R == (n' == 0) && Immutability
    boolean isEmpty();

    // Pred: true
    // Post: n == 0
    void clear();

    // Pred: k > 0
    // Post: R == [ q'_i : 1 <= i <= n' && i mod k == 0 ]
    //       && Immutability
    Queue getNth(int k);

    // Pred: k > 0
    // Post: R == [ q'_i : 1 <= i <= n' && i mod k == 0 ]
    //       && n == n' / k && q == [ q'_i : 1 <= i <= n' && i mod k != 0 ]
    Queue removeNth(int k);

    // Pred: k > 0
    // Post: n == n' / k && q == [ q'_i : 1 <= i <= n' && i mod k != 0 ]
    void dropNth(int k);
}
