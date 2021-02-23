package queue;

// Model:
// [x_1, x_2, ..., x_n]
// n -- capacity of queue

// Inv:
// n >= 0 && forall i = 1, ..., n : x_i != null

public class ArrayQueue {
    private int n = 0, l = 0, r = -1;
    private Object[] a = new Object[2];

    // Pred: x != null
    // Post: this.n == this.n' + 1 && this.x_n == x
    //       && forall i = 1, ..., this.n' : this.x_i == this.x'_i
    public void enqueue(Object x) {
        ensureCapacity(n + 1);
        r = r + 1 < a.length ? r + 1 : 0;
        a[r] = x;
        n++;
    }

    // Pred: true
    // Post: R == this.x'_1 && this.n == this.n'
    //       && forall i = 1, ..., this.n' : this.x_i == this.x'_i
    public Object element() {
        return a[l];
    }

    // Pred: this.n > 0
    // Post: R == this.x'_1 && this.n == this.n' - 1
    //       && forall i = 2, ..., this.n' : this.x_(i - 1) == this.x'_i
    public Object dequeue() {
        Object tmp = a[l];
        a[l] = null;
        l = l + 1 < a.length ? l + 1 : 0;
        n--;
        return tmp;
    }

    // Pred: true
    // Post: R == this.n' && this.n == this.n'
    //       && forall i = 1, ..., this.n' : this.x_i == this.x'_i
    public int size() {
        return n;
    }

    // Pred: true
    // Post: R == (this.n' == 0) && this.n == this.n'
    //       && forall i = 1, ..., this.n' : this.x_i == this.x'_i
    public boolean isEmpty() {
        return n == 0;
    }

    // Pred: true
    // Post: this.n == 0
    public void clear() {
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

    private void ensureCapacity(int capacity) {
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
