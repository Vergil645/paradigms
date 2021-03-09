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
        n--;
        return dequeueImpl();
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
        clearImpl();
        n = 0;
    }

    protected abstract void enqueueImpl(Object x);

    protected abstract Object elementImpl();

    protected abstract Object dequeueImpl();

    protected abstract void clearImpl();
}
