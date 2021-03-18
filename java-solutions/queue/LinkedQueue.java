package queue;

public class LinkedQueue extends AbstractQueue {
    private static class Node {
        private Object value;
        private Node right;

        Node(Object value) {
            this.value = value;
            this.right = null;
        }
    }

    private Node head, tail;

    // Pred: true
    // Post: this.n == 0
    public LinkedQueue() {
        n = 0;
        head = null;
        tail = null;
    }

    @Override
    protected void enqueueImpl(Object x) {
        if (n == 0) {
            head = new Node(x);
            tail = head;
        } else {
            tail.right = new Node(x);
            tail = tail.right;
        }
    }

    @Override
    protected Object elementImpl() {
        return head.value;
    }

    @Override
    protected void deleteHead() {
        Node tmp = head.right;
        head.value = null;
        head.right = null;
        head = tmp;
        if (head == null) {
            tail = null;
        }
        n--;
    }

    @Override
    protected Queue getInstance() {
        return new LinkedQueue();
    }
}
