package queue;

// Model:
// [q_1, q_2, ..., q_n]
// n -- capacity of queue

// Inv:
// n >= 0 && forall i = 1, ..., n : q[i] != null

// Immutability(queue):
// queue.n == queue.n' && forall i = 1, ..., queue.n' : queue.q[i] == queue.q'[i]

import java.util.Arrays;
import java.util.Objects;

public class ArrayQueueADT {
    private int n = 0, l = 0;
    private Object[] a = new Object[2];

    // Pred: queue != null && x != null
    // Post: queue.n == queue.n' + 1 && queue.q[n] == x
    //       && forall i = 1, ..., queue.n' : queue.q[i] == queue.q'[i]
    public static void enqueue(ArrayQueueADT queue, Object x) {
        Objects.requireNonNull(queue);
        Objects.requireNonNull(x);
        ensureCapacity(queue, queue.n + 1);
        queue.a[(queue.l + queue.n) % queue.a.length] = x;
        queue.n++;
    }

    // Pred: queue != null && x != null
    // Post: queue.n == queue.n' + 1 && queue.q[1] == x
    //       && forall i = 2, ..., queue.n : queue.q[i] == queue.q'[i - 1]
    public static void push(ArrayQueueADT queue, Object x) {
        Objects.requireNonNull(queue);
        Objects.requireNonNull(x);
        ensureCapacity(queue, queue.n + 1);
        queue.l = (queue.l - 1 + queue.a.length) % queue.a.length;
        queue.a[queue.l] = x;
        queue.n++;
    }

    // Pred: queue != null && queue.n > 0
    // Post: R == queue.q'[1] && Immutability(queue)
    public static Object element(ArrayQueueADT queue) {
        Objects.requireNonNull(queue);
        assert queue.n > 0;
        return queue.a[queue.l];
    }

    // Pred: queue != null && queue.n > 0
    // Post: R == queue.q'[queue.n'] && Immutability(queue)
    public static Object peek(ArrayQueueADT queue) {
        Objects.requireNonNull(queue);
        assert queue.n > 0;
        return queue.a[(queue.l + queue.n - 1) % queue.a.length];
    }

    // Pred: queue != null && queue.n > 0
    // Post: R == queue.q'[1] && queue.n == queue.n' - 1
    //       && forall i = 2, ..., queue.n' : queue.q[i - 1] == queue.q'[i]
    public static Object dequeue(ArrayQueueADT queue) {
        Objects.requireNonNull(queue);
        assert queue.n > 0;
        Object tmp = queue.a[queue.l];
        queue.a[queue.l] = null;
        queue.l = (queue.l + 1) % queue.a.length;
        queue.n--;
        return tmp;
    }

    // Pred: queue != null && queue.n > 0
    // Post: R == queue.q'[queue.n'] && queue.n == queue.n' - 1
    //       && forall i = 1, ..., queue.n : queue.q[i] == queue.q'[i]
    public static Object remove(ArrayQueueADT queue) {
        Objects.requireNonNull(queue);
        assert queue.n > 0;
        queue.n--;
        Object tmp = queue.a[(queue.l + queue.n) % queue.a.length];
        queue.a[(queue.l + queue.n) % queue.a.length] = null;
        return tmp;
    }

    // Pred: queue != null
    // Post: R == queue.q'.toArray() && Immutability(queue)
    public static Object[] toArray(ArrayQueueADT queue) {
        Objects.requireNonNull(queue);
        return rebuild(queue, queue.n);
    }

    // Pred: queue != null
    // Post: R == queue.q'.toString() && Immutability(queue)
    public static String toStr(ArrayQueueADT queue) {
        Objects.requireNonNull(queue);
        StringBuilder str = new StringBuilder().append('[');
        for (int i = 0; i < queue.n - 1; i++) {
            str.append(queue.a[(queue.l + i) % queue.a.length]).append(", ");
        }
        if (queue.n > 0) {
            str.append(queue.a[(queue.l + queue.n - 1) % queue.a.length]);
        }
        return str.append(']').toString();
    }

    // Pred: queue != null
    // Post: R == queue.n' && Immutability(queue)
    public static int size(ArrayQueueADT queue) {
        Objects.requireNonNull(queue);
        return queue.n;
    }

    // Pred: queue != null
    // Post: R == (queue.n' == 0) && Immutability(queue)
    public static boolean isEmpty(ArrayQueueADT queue) {
        Objects.requireNonNull(queue);
        return queue.n == 0;
    }

    // Pred: queue != null
    // Post: queue.n == 0
    public static void clear(ArrayQueueADT queue) {
        Objects.requireNonNull(queue);
        if (queue.l + queue.n - 1 < queue.a.length) {
            Arrays.fill(queue.a, queue.l, queue.l + queue.n, null);
        } else {
            Arrays.fill(queue.a, queue.l, queue.a.length, null);
            Arrays.fill(queue.a, 0, queue.l + queue.n - queue.a.length, null);
        }
        queue.l = 0;
        queue.n = 0;
    }

    private static void ensureCapacity(ArrayQueueADT queue, int capacity) {
        if (capacity > queue.a.length) {
            queue.a = rebuild(queue, 2 * capacity);
            queue.l = 0;
        }
    }

    private static Object[] rebuild(ArrayQueueADT queue, int capacity) {
        Object[] array = new Object[capacity];
        if (queue.l + queue.n - 1 < queue.a.length) {
            System.arraycopy(queue.a, queue.l, array, 0, queue.n);
        } else {
            System.arraycopy(queue.a, queue.l, array, 0, queue.a.length - queue.l);
            System.arraycopy(queue.a, 0,
                    array, queue.a.length - queue.l,
                    queue.l + queue.n - queue.a.length);
        }
        return array;
    }
}
