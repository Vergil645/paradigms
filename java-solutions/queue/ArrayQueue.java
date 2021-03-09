package queue;

import java.util.Arrays;
import java.util.Objects;

public class ArrayQueue extends AbstractQueue {
    private int l;
    private Object[] a;

    // Pred: true
    // Post: this.n == 0
    public ArrayQueue() {
        l = 0;
        a = new Object[2];
    }

    @Override
    protected void enqueueImpl(Object x) {
        ensureCapacity(n + 1);
        a[(l + n) % a.length] = x;
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

    @Override
    protected Object elementImpl() {
        return a[l];
    }

    // Pred: this.n > 0
    // Post: R == this.q'[this.n'] && Immutability(this)
    public Object peek() {
        assert n > 0;
        return a[(l + n - 1) % a.length];
    }

    @Override
    protected Object dequeueImpl() {
        Object tmp = a[l];
        a[l] = null;
        l = (l + 1) % a.length;
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
            str.append(a[(l + n - 1) % a.length]);
        }
        return str.append(']').toString();
    }

    @Override
    protected void clearImpl() {
        if (l + n - 1 < a.length) {
            Arrays.fill(a, l, l + n, null);
        } else {
            Arrays.fill(a, l, a.length, null);
            Arrays.fill(a, 0, l + n - a.length, null);
        }
        l = 0;
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
