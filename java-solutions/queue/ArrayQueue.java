package queue;

// Model:
// [q_1, q_2, ..., q_n]
// n -- capacity of queue

// Inv:
// n >= 0 && forall i = 1, ..., n : q[i] != null

// Immutability(this):
// this.n == this.n' && forall i = 1, ..., this.n' : this.q[i] == this.q'[i]

import java.util.Arrays;
import java.util.Objects;

public class ArrayQueue {
    private int n, l;
    private Object[] a;

    // Pred: true
    // Post: this.n == 0
    public ArrayQueue() {
        n = 0;
        l = 0;
        a = new Object[2];
    }

    // Pred: x != null
    // Post: this.n == this.n' + 1 && this.q[n] == x
    //       && forall i = 1, ..., this.n' : this.q[i] == this.q'[i]
    public void enqueue(Object x) {
        Objects.requireNonNull(x);
        ensureCapacity(n + 1);
        a[(l + n) % a.length] = x;
        n++;
    }

    // Pred: x != null
    // Post: this.n == this.n' + 1 && this.q[1] == x
    //       && forall i = 2, ..., this.n : this.q[i] == this.q'[i - 1]
    public void push(Object x) {
        Objects.requireNonNull(x);
        ensureCapacity(n + 1);
        l = (l - 1 + a.length) % a.length;
        a[l] = x;
        n++;
    }

    // Pred: this.n > 0
    // Post: R == this.q'[1] && Immutability(this)
    public Object element() {
        assert n > 0;
        return a[l];
    }

    // Pred: this.n > 0
    // Post: R == this.q'[this.n'] && Immutability(this)
    public Object peek() {
        assert n > 0;
        return a[(l + n - 1) % a.length];
    }

    // Pred: this.n > 0
    // Post: R == this.q'[1] && this.n == this.n' - 1
    //       && forall i = 2, ..., this.n' : this.q[i - 1] == this.q'[i]
    public Object dequeue() {
        assert n > 0;
        Object tmp = a[l];
        a[l] = null;
        l = (l + 1) % a.length;
        n--;
        return tmp;
    }

    // Pred: this.n > 0
    // Post: R == this.q'[this.n'] && this.n == this.n' - 1
    //       && forall i = 1, ..., this.n : this.q[i] == this.q'[i]
    public Object remove() {
        assert n > 0;
        n--;
        Object tmp = a[(l + n) % a.length];
        a[(l + n) % a.length] = null;
        return tmp;
    }

    // Pred: true
    // Post: R == this.q'.toArray() && Immutability(this)
    public Object[] toArray() {
        return rebuild(n);
    }

    // Pred: true
    // Post: R == this.q'.toString() && Immutability(this)
    public String toStr() {
        StringBuilder str = new StringBuilder().append('[');
        for (int i = 0; i < n - 1; i++) {
            str.append(a[(l + i) % a.length]).append(", ");
        }
        if (n > 0) {
            return str.append(a[(l + n - 1) % a.length]).append(']').toString();
        } else {
            return str.append(']').toString();
        }
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
        if (l + n - 1 < a.length) {
            Arrays.fill(a, l, l + n, null);
        } else {
            Arrays.fill(a, l, a.length, null);
            Arrays.fill(a, 0, l + n - a.length, null);
        }
        l = 0;
        n = 0;
    }

    private void ensureCapacity(int capacity) {
        if (capacity > a.length) {
            a = rebuild(2 * capacity);
            l = 0;
        }
    }

    private Object[] rebuild(int capacity) {
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
