package queue;

import java.util.Arrays;

public class Main {
    private static void showArrayQueueModule() {
        System.out.println("================");
        System.out.println("ArrayQueueModule");
        System.out.println("================\n");
        
        for (int i = 0; i < 10; i++) {
            ArrayQueueModule.enqueue(i);
        }
        for (int i = 1; i < 10; i++) {
            ArrayQueueModule.push(-i);
        }
        System.out.println(ArrayQueueModule.isEmpty());
        System.out.println(ArrayQueueModule.element());
        System.out.println(ArrayQueueModule.peek());

        System.out.println(ArrayQueueModule.dequeue());
        System.out.println(ArrayQueueModule.remove());
        System.out.println(ArrayQueueModule.size());

        System.out.println(Arrays.toString(ArrayQueueModule.toArray()));
        System.out.println(ArrayQueueModule.toStr());

        ArrayQueueModule.clear();
        System.out.println(ArrayQueueModule.isEmpty());
        System.out.println();
    }

    private static void showArrayQueueADT() {
        System.out.println("=============");
        System.out.println("ArrayQueueADT");
        System.out.println("=============\n");

        ArrayQueueADT queue1 = new ArrayQueueADT();
        ArrayQueueADT queue2 = new ArrayQueueADT();

        testArrayQueueADT(queue1, 0);

        testArrayQueueADT(queue2, 10);
    }

    private static void testArrayQueueADT(ArrayQueueADT queue, int x) {
        for (int i = x; i < x + 10; i++) {
            ArrayQueueADT.enqueue(queue, i);
        }
        for (int i = x + 1; i < x + 10; i++) {
            ArrayQueueADT.push(queue, -i);
        }
        System.out.println(ArrayQueueADT.isEmpty(queue));
        System.out.println(ArrayQueueADT.element(queue));
        System.out.println(ArrayQueueADT.peek(queue));

        System.out.println(ArrayQueueADT.dequeue(queue));
        System.out.println(ArrayQueueADT.remove(queue));
        System.out.println(ArrayQueueADT.size(queue));

        System.out.println(Arrays.toString(ArrayQueueADT.toArray(queue)));
        System.out.println(ArrayQueueADT.toStr(queue));

        ArrayQueueADT.clear(queue);
        System.out.println(ArrayQueueADT.isEmpty(queue));
        System.out.println();
    }

    private static void showArrayQueue() {
        System.out.println("==========");
        System.out.println("ArrayQueue");
        System.out.println("==========\n");

        ArrayQueue queue1 = new ArrayQueue();
        ArrayQueue queue2 = new ArrayQueue();

        testArrayQueue(queue1, 0);

        testArrayQueue(queue2, 10);
    }

    private static void testArrayQueue(ArrayQueue queue, int x) {
        for (int i = x; i < x + 10; i++) {
            queue.enqueue(i);
        }
        for (int i = x + 1; i < x + 10; i++) {
            queue.push(-i);
        }
        System.out.println(queue.isEmpty());
        System.out.println(queue.element());
        System.out.println(queue.peek());

        System.out.println(queue.dequeue());
        System.out.println(queue.remove());
        System.out.println(queue.size());

        System.out.println(Arrays.toString(queue.toArray()));
        System.out.println(queue.toStr());

        queue.clear();
        System.out.println(queue.isEmpty());
        System.out.println();
    }

    public static void main(String[] args) {
        showArrayQueueModule();

        showArrayQueueADT();

        showArrayQueue();
    }
}
