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
        System.out.println(ArrayQueueModule.dequeue());
        System.out.println(ArrayQueueModule.remove());
        System.out.println(ArrayQueueModule.size());
        System.out.println(Arrays.toString(ArrayQueueModule.toArray()));
        System.out.println(ArrayQueueModule.toStr());
        ArrayQueueModule.clear();
        System.out.println(ArrayQueueModule.size());
        System.out.println();
    }

    private static void showArrayQueueADT() {
        System.out.println("=============");
        System.out.println("ArrayQueueADT");
        System.out.println("=============\n");

        ArrayQueueADT queue1 = new ArrayQueueADT();
        ArrayQueueADT queue2 = new ArrayQueueADT();

        for (int i = 0; i < 10; i++) {
            ArrayQueueADT.enqueue(queue1, i);
        }
        for (int i = 1; i < 10; i++) {
            ArrayQueueADT.push(queue1, -i);
        }
        System.out.println(ArrayQueueADT.dequeue(queue1));
        System.out.println(ArrayQueueADT.remove(queue1));
        System.out.println(ArrayQueueADT.size(queue1));
        System.out.println(Arrays.toString(ArrayQueueADT.toArray(queue1)));
        System.out.println(ArrayQueueADT.toStr(queue1));
        ArrayQueueADT.clear(queue1);
        System.out.println(ArrayQueueADT.size(queue1));
        System.out.println();

        for (int i = 10; i < 20; i++) {
            ArrayQueueADT.enqueue(queue2, i);
        }
        for (int i = 11; i < 20; i++) {
            ArrayQueueADT.push(queue2, -i);
        }
        System.out.println(ArrayQueueADT.dequeue(queue2));
        System.out.println(ArrayQueueADT.remove(queue2));
        System.out.println(ArrayQueueADT.size(queue2));
        System.out.println(Arrays.toString(ArrayQueueADT.toArray(queue2)));
        System.out.println(ArrayQueueADT.toStr(queue2));
        ArrayQueueADT.clear(queue2);
        System.out.println(ArrayQueueADT.size(queue2));
        System.out.println();
    }

    private static void showArrayQueue() {
        System.out.println("==========");
        System.out.println("ArrayQueue");
        System.out.println("==========\n");

        ArrayQueue queue1 = new ArrayQueue();
        ArrayQueue queue2 = new ArrayQueue();

        for (int i = 0; i < 10; i++) {
            queue1.enqueue(i);
        }
        for (int i = 1; i < 10; i++) {
            queue1.push(-i);
        }
        System.out.println(queue1.dequeue());
        System.out.println(queue1.remove());
        System.out.println(queue1.size());
        System.out.println(Arrays.toString(queue1.toArray()));
        System.out.println(queue1.toStr());
        queue1.clear();
        System.out.println(queue1.size());
        System.out.println();

        for (int i = 10; i < 20; i++) {
            queue2.enqueue(i);
        }
        for (int i = 11; i < 20; i++) {
            queue2.push(-i);
        }
        System.out.println(queue2.dequeue());
        System.out.println(queue2.remove());
        System.out.println(queue2.size());
        System.out.println(Arrays.toString(queue2.toArray()));
        System.out.println(queue2.toStr());
        queue2.clear();
        System.out.println(queue2.size());
        System.out.println();
    }

    public static void main(String[] args) {
        showArrayQueueModule();

        showArrayQueueADT();

        showArrayQueue();
    }
}
