package queue;

import java.util.Objects;

public abstract class AbstractQueue implements Queue {
    protected int n;

    @Override
    public void enqueue(Object x) {
        Objects.requireNonNull(x);
        enqueueImpl(x);
        n++;
    }

    @Override
    public Object element() {
        assert n > 0;
        return elementImpl();
    }

    @Override
    public Object dequeue() {
        assert n > 0;
        Object tmp = elementImpl();
        deleteHead();
        return tmp;
    }

    @Override
    public int size() {
        return n;
    }

    @Override
    public boolean isEmpty() {
        return n == 0;
    }

    @Override
    public void clear() {
        while (n > 0) {
            deleteHead();
        }
    }

    @Override
    public Queue getNth(int k) {
        assert k > 0;
        return getNthImpl(k);
    }

    @Override
    public Queue removeNth(int k) {
        assert k > 0;
        // :NOTE: два прохода + можно вынести больше общего кода
        Queue tmp = getNth(k);
        dropNth(k);
        return tmp;
    }

    @Override
    public void dropNth(int k) {
        assert k > 0;
        int lastN = n;
        for (int i = 0; i < lastN; i++) {
            if (i % k == k - 1) {
                deleteHead();
            } else {
                enqueue(dequeue());
            }
        }
    }

    protected abstract void enqueueImpl(Object x);

    protected abstract Object elementImpl();

    protected abstract void deleteHead();

    protected abstract Queue getNthImpl(int k);
}
