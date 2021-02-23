package queue;

// Model:
// [x_1, x_2, ..., x_n]
// n -- capacity of queue

// Inv:
// n >= 0 && forall i = 1, ..., n : x_i != null
// && [ (l == 0 && r == -1)
//      || (0 <= l <= r && r - l + 1 == n && forall i = l, ..., r : a[i] == x_(i - l + 1))
//      || (0 <= r < l && a.length - l + 1 + r == n
//          && forall i = l, ..., a.length - 1 : a[i] == x_(i - l + 1)
//          && forall i = 0, ..., r : a[i] == x_(a.length - l + 1 + i)) ]

public class ArrayQueueModule {
    private static int n = 0, l = 0, r = -1;
    private static Object[] a = new Object[2];

    // Pred: x != null
    // Post: n == n' + 1 && x_n == x && forall i = 1, ..., n' : x_i == x'_i
    public static void enqueue(Object x) {
        ensureCapacity(n + 1);
        r = r + 1 < a.length ? r + 1 : 0;
        a[r] = x;
        n++;
    }

    // Pred: true
    // Post: R == x'_1 && n == n' && forall i = 1, ..., n' : x_i == x'_i
    public static Object element() {
        return a[l];
    }

    // Pred: n > 0
    // Post: R == x'_1 && n == n' - 1 && forall i = 2, ..., n' : x_(i - 1) == x'_i
    public static Object dequeue() {
        Object tmp = a[l];
        a[l] = null;
        l = l + 1 < a.length ? l + 1 : 0;
        n--;
        return tmp;
    }

    // Pred: true
    // Post: R == n' && n == n' && forall i = 1, ..., n' : x_i == x'_i
    public static int size() {
        return n;
    }

    // Pred: true
    // Post: R == (n' == 0) && n == n' && forall i = 1, ..., n' : x_i == x'_i
    public static boolean isEmpty() {
        return n == 0;
    }

    // Pred: true
    // Post: n == 0
    public static void clear() {
        if (r < l) {
            for (int i = l; i < a.length; i++) {
                a[i] = null;
            }
            for (int i = 0; i <= r; i++) {
                a[i] = null;
            }
        } else {
            for (int i = l; i <= r; i++) {
                a[i] = null;
            }
        }
        l = 0;
        r = -1;
        n = 0;
    }

    private static void ensureCapacity(int capacity) {
        if (capacity > a.length) {
            Object[] tmp = new Object[capacity * 2];
            int k = 0;
            if (r < l) {
                for (int i = l; i < a.length; i++) {
                    tmp[k++] = a[i];
                }
                for (int i = 0; i <= r; i++) {
                    tmp[k++] = a[i];
                }
            } else {
                for (int i = l; i <= r; i++) {
                    tmp[k++] = a[i];
                }
            }
            a = tmp;
            l = 0;
            r = n - 1;
        }
    }
}
