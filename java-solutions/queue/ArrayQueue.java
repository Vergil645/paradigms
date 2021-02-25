package queue;

// Model:
// [q_1, q_2, ..., q_n]
// n -- capacity of queue

// Inv:
// n >= 0 && forall i = 1, ..., n : q[i] != null

// Immutability(this):
// this.n == this.n' && forall i = 1, ..., this.n' : this.q[i] == this.q'[i]

public class ArrayQueue {
    private int n = 0, l = 0, r = -1;
    private Object[] a = new Object[2];

    // Pred: x != null
    // Post: this.n == this.n' + 1 && this.q[n] == x
    //       && forall i = 1, ..., this.n' : this.q[i] == this.q'[i]
    public void enqueue(Object x) {
        ensureCapacity(n + 1);
        r = r + 1 < a.length ? r + 1 : 0;
        a[r] = x;
        n++;
    }

    // Pred: true
    // Post: R == this.q'[1] && Immutability(this)
    public Object element() {
        return a[l];
    }

    // Pred: this.n > 0
    // Post: R == this.q'[1] && this.n == this.n' - 1
    //       && forall i = 2, ..., this.n' : this.q[i - 1] == this.q'[i]
    public Object dequeue() {
        Object tmp = a[l];
        a[l] = null;
        l = l + 1 < a.length ? l + 1 : 0;
        n--;
        return tmp;
    }

    // Pred: true
    // Post: R == this.n' && Immutability(this)
    public int size() {
        return n;
    }

    // Pred: true
    // Post: R == (this.n' == 0) && Immutability(this)
    public boolean isEmpty() {
        return n == 0;
    }

    // Pred: true
    // Post: this.n == 0
    public void clear() {
        for (int i = 0; i < n; i++) {
            a[(l + i) % a.length] = null;
        }
        l = 0;
        r = -1;
        n = 0;
    }

    private void ensureCapacity(int capacity) {
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
