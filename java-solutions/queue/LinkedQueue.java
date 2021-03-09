package queue;

public class LinkedQueue extends AbstractQueue {
    private static class Node {
        private final Object x;
        private Node right;

        Node(Object x) {
            this.x = x;
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
        return head.x;
    }

    @Override
    protected Object dequeueImpl() {
        Object res = head.x;
        deleteHead();
        return res;
    }

    @Override
    protected void clearImpl() {
        while (head != null) {
            deleteHead();
        }
    }

    private void deleteHead() {
        Node tmp = head.right;
        head.right = null;
        head = tmp;
        if (head == null) {
            tail = null;
        }
    }
}
