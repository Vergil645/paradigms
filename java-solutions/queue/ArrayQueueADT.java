package queue;

// Model:
// [x_1, x_2, ..., x_n]
// n -- capacity of queue

// Inv:
// n >= 0 && forall i = 1, ..., n : x_i != null

public class ArrayQueueADT {
    private int n = 0, l = 0, r = -1;
    private Object[] a = new Object[2];

    // Pred: queue != null && x != null
    // Post: queue.n == queue.n' + 1 && queue.x_n == x
    //       && forall i = 1, ..., queue.n' : queue.x_i == queue.x'_i
    public static void enqueue(ArrayQueueADT queue, Object x) {
        ensureCapacity(queue, queue.n + 1);
        queue.r = queue.r + 1 < queue.a.length ? queue.r + 1 : 0;
        queue.a[queue.r] = x;
        queue.n++;
    }

    // Pred: queue != null
    // Post: R == queue.x'_1 && queue.n == queue.n'
    //       && forall i = 1, ..., queue.n' : queue.x_i == queue.x'_i
    public static Object element(ArrayQueueADT queue) {
        return queue.a[queue.l];
    }

    // Pred: queue != null && queue.n > 0
    // Post: R == queue.x'_1 && queue.n == queue.n' - 1
    //       && forall i = 2, ..., queue.n' : queue.x_(i - 1) == queue.x'_i
    public static Object dequeue(ArrayQueueADT queue) {
        Object tmp = queue.a[queue.l];
        queue.a[queue.l] = null;
        queue.l = queue.l + 1 < queue.a.length ? queue.l + 1 : 0;
        queue.n--;
        return tmp;
    }

    // Pred: queue != null
    // Post: R == queue.n' && queue.n == queue.n'
    //       && forall i = 1, ..., queue.n' : queue.x_i == queue.x'_i
    public static int size(ArrayQueueADT queue) {
        return queue.n;
    }

    // Pred: queue != null
    // Post: R == (queue.n' == 0) && queue.n == queue.n'
    //       && forall i = 1, ..., queue.n' : queue.x_i == queue.x'_i
    public static boolean isEmpty(ArrayQueueADT queue) {
        return queue.n == 0;
    }

    // Pred: queue != null
    // Post: queue.n == 0
    public static void clear(ArrayQueueADT queue) {
        if (queue.r < queue.l) {
            for (int i = queue.l; i < queue.a.length; i++) {
                queue.a[i] = null;
            }
            for (int i = 0; i <= queue.r; i++) {
                queue.a[i] = null;
            }
        } else {
            for (int i = queue.l; i <=queue.r; i++) {
                queue.a[i] = null;
            }
        }
        queue.l = 0;
        queue.r = -1;
        queue.n = 0;
    }

    private static void ensureCapacity(ArrayQueueADT queue, int capacity) {
        if (capacity > queue.a.length) {
            Object[] tmp = new Object[capacity * 2];
            int k = 0;
            if (queue.r < queue.l) {
                for (int i = queue.l; i < queue.a.length; i++) {
                    tmp[k++] = queue.a[i];
                }
                for (int i = 0; i <= queue.r; i++) {
                    tmp[k++] = queue.a[i];
                }
            } else {
                for (int i = queue.l; i <= queue.r; i++) {
                    tmp[k++] = queue.a[i];
                }
            }
            queue.a = tmp;
            queue.l = 0;
            queue.r = queue.n - 1;
        }
    }
}
