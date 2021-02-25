package queue;

// Model:
// [q_1, q_2, ..., q_n]
// n -- capacity of queue

// Inv:
// n >= 0 && forall i = 1, ..., n : q[i] != null

// Immutability(queue):
// queue.n == queue.n' && forall i = 1, ..., queue.n' : queue.q[i] == queue.q'[i]

public class ArrayQueueADT {
    private int n = 0, l = 0, r = -1;
    private Object[] a = new Object[2];

    // Pred: queue != null && x != null
    // Post: queue.n == queue.n' + 1 && queue.q[n] == x
    //       && forall i = 1, ..., queue.n' : queue.q[i] == queue.q'[i]
    public static void enqueue(ArrayQueueADT queue, Object x) {
        ensureCapacity(queue, queue.n + 1);
        queue.r = queue.r + 1 < queue.a.length ? queue.r + 1 : 0;
        queue.a[queue.r] = x;
        queue.n++;
    }

    // Pred: queue != null
    // Post: R == queue.q'[1] && Immutability(queue)
    public static Object element(ArrayQueueADT queue) {
        return queue.a[queue.l];
    }

    // Pred: queue != null && queue.n > 0
    // Post: R == queue.q'[1] && queue.n == queue.n' - 1
    //       && forall i = 2, ..., queue.n' : queue.q[i - 1] == queue.q'[i]
    public static Object dequeue(ArrayQueueADT queue) {
        Object tmp = queue.a[queue.l];
        queue.a[queue.l] = null;
        queue.l = queue.l + 1 < queue.a.length ? queue.l + 1 : 0;
        queue.n--;
        return tmp;
    }

    // Pred: queue != null
    // Post: R == queue.n' && Immutability(queue)
    public static int size(ArrayQueueADT queue) {
        return queue.n;
    }

    // Pred: queue != null
    // Post: R == (queue.n' == 0) && Immutability(queue)
    public static boolean isEmpty(ArrayQueueADT queue) {
        return queue.n == 0;
    }

    // Pred: queue != null
    // Post: queue.n == 0
    public static void clear(ArrayQueueADT queue) {
        for (int i = 0; i < queue.n; i++) {
            queue.a[(queue.l + i) % queue.a.length] = null;
        }
        queue.l = 0;
        queue.r = -1;
        queue.n = 0;
    }

    private static void ensureCapacity(ArrayQueueADT queue, int capacity) {
        if (capacity > queue.a.length) {
            Object[] tmp = new Object[capacity * 2];
            for (int i = 0; i < queue.n; i++) {
                tmp[i] = queue.a[(queue.l + i) % queue.a.length];
            }
            queue.a = tmp;
            queue.l = 0;
            queue.r = queue.n - 1;
        }
    }
}
