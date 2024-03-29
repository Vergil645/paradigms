package queue;

// Model:
// [q_1, q_2, ..., q_n]
// n -- capacity of queue

// Inv:
// n >= 0 && forall i = 1, ..., n : q[i] != null

// Immutability:
// n == n' && forall i = 1, ..., n' : q[i] == q'[i]

import java.util.Arrays;
import java.util.Objects;

public class ArrayQueueModule {
    private static int n = 0, l = 0;
    private static Object[] a = new Object[2];

    // Pred: x != null
    // Post: n == n' + 1 && q[n] == x && forall i = 1, ..., n' : q[i] == q'[i]
    public static void enqueue(Object x) {
        Objects.requireNonNull(x);
        ensureCapacity(n + 1);
        a[(l + n) % a.length] = x;
        n++;
    }

    // Pred: x != null
    // Post: n == n' + 1 && q[1] == x && forall i = 2, ..., n : q[i] == q'[i - 1]
    public static void push(Object x) {
        Objects.requireNonNull(x);
        ensureCapacity(n + 1);
        l = (l - 1 + a.length) % a.length;
        a[l] = x;
        n++;
    }

    // Pred: n > 0
    // Post: R == q'[1] && Immutability
    public static Object element() {
        assert n > 0;
        return a[l];
    }

    // Pred: n > 0
    // Post: R == q'[n'] && Immutability
    public static Object peek() {
        assert n > 0;
        return a[(l + n - 1) % a.length];
    }

    // Pred: n > 0
    // Post: R == q'[1] && n == n' - 1 && forall i = 2, ..., n' : q[i - 1] == q'[i]
    public static Object dequeue() {
        assert n > 0;
        Object tmp = a[l];
        a[l] = null;
        l = (l + 1) % a.length;
        n--;
        return tmp;
    }

    // Pred: n > 0
    // Post: R == q'[n'] && n == n' - 1 && forall i = 1, ..., n : q[i] == q'[i]
    public static Object remove() {
        assert n > 0;
        n--;
        Object tmp = a[(l + n) % a.length];
        a[(l + n) % a.length] = null;
        return tmp;
    }

    // Pred: true
    // Post: R == q'.toArray() && Immutability
    public static Object[] toArray() {
        return rebuild(n);
    }

    // Pred: true
    // Post: R == q'.toString() && Immutability
    public static String toStr() {
        StringBuilder str = new StringBuilder().append('[');
        for (int i = 0; i < n - 1; i++) {
            str.append(a[(l + i) % a.length]).append(", ");
        }
        if (n > 0) {
            str.append(a[(l + n - 1) % a.length]);
        }
        return str.append(']').toString();
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
        if (l + n - 1 < a.length) {
            Arrays.fill(a, l, l + n, null);
        } else {
            Arrays.fill(a, l, a.length, null);
            Arrays.fill(a, 0, l + n - a.length, null);
        }
        l = 0;
        n = 0;
    }

    private static void ensureCapacity(int capacity) {
        if (capacity > a.length) {
            a = rebuild(2 * capacity);
            l = 0;
        }
    }

    private static Object[] rebuild(int capacity) {
        Object[] array = new Object[capacity];
        if (l + n - 1 < a.length) {
            System.arraycopy(a, l, array, 0, n);
        } else {
            System.arraycopy(a, l, array, 0, a.length - l);
            System.arraycopy(a, 0, array, a.length - l, l + n - a.length);
        }
        return array;
    }
}
