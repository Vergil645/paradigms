package queue;

// Model:
// [q_1, q_2, ..., q_n]
// n -- capacity of queue

// Inv:
// n >= 0 && forall i = 1, ..., n : q[i] != null

// Immutability:
// n == n' && forall i = 1, ..., n' : q[i] == q'[i]

public class ArrayQueueModule {
    private static int n = 0, l = 0, r = -1;
    private static Object[] a = new Object[2];

    // Pred: x != null
    // Post: n == n' + 1 && q[n] == x && forall i = 1, ..., n' : q[i] == q'[i]
    public static void enqueue(Object x) {
        ensureCapacity(n + 1);
        r = r + 1 < a.length ? r + 1 : 0;
        a[r] = x;
        n++;
    }

    // Pred: true
    // Post: R == q'[1] && Immutability
    public static Object element() {
        return a[l];
    }

    // Pred: n > 0
    // Post: R == q'[1] && n == n' - 1 && forall i = 2, ..., n' : q[i - 1] == q'[i]
    public static Object dequeue() {
        Object tmp = a[l];
        a[l] = null;
        l = l + 1 < a.length ? l + 1 : 0;
        n--;
        return tmp;
    }

    // Pred: true
    // Post: R == n' && Immutability
    public static int size() {
        return n;
    }

    // Pred: true
    // Post: R == (n' == 0) && Immutability
    public static boolean isEmpty() {
        return n == 0;
    }

    // Pred: true
    // Post: n == 0
    public static void clear() {
        for (int i = 0; i < n; i++) {
            a[(l + i) % a.length] = null;
        }
        l = 0;
        r = -1;
        n = 0;
    }

    private static void ensureCapacity(int capacity) {
        if (capacity > a.length) {
            Object[] tmp = new Object[capacity * 2];
            for (int i = 0; i < n; i++) {
                tmp[i] = a[(l + i) % a.length];
            }
            a = tmp;
            l = 0;
            r = n - 1;
        }
    }
}
