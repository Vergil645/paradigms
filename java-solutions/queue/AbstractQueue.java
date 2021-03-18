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
        Queue tmp = getInstance();
        findNth(k, false, tmp);
        return tmp;
    }

    @Override
    public Queue removeNth(int k) {
        assert k > 0;
        Queue tmp = getInstance();
        findNth(k, true, tmp);
        return tmp;
    }

    @Override
    public void dropNth(int k) {
        assert k > 0;
        findNth(k, true, null);
    }

    private void findNth(int k, boolean removable, Queue dst) {
        int lastN = n;
        for (int i = 1; i <= lastN; i++) {
            if (i % k == 0) {
                if (dst != null) {
                    dst.enqueue(element());
                }
                if (removable) {
                    deleteHead();
                    continue;
                }
            }
            enqueue(dequeue());
        }
    }

    protected abstract void enqueueImpl(Object x);

    protected abstract Object elementImpl();

    protected abstract void deleteHead();

    protected abstract Queue getInstance();
}
