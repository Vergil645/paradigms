package queue;

public class Main {
    private static void showArrayQueueModule() {
        System.out.println("================");
        System.out.println("ArrayQueueModule");
        System.out.println("================\n");

        System.out.print("Add to Queue elements: ");
        for (int x = 1; x <= 10; x++) {
            System.out.print(x + " ");
            ArrayQueueModule.enqueue(x);
        }
        System.out.println("\nQueue size: " + ArrayQueueModule.size() + "\n");

        System.out.print("Extract first 5 elements: ");
        for (int i = 1; i <= 5; i++) {
            System.out.print(ArrayQueueModule.dequeue() + " ");
        }
        System.out.println("\nQueue size: " + ArrayQueueModule.size() + "\n");

        System.out.print("Add to Queue elements: ");
        for (int x = 11; x <= 20; x++) {
            System.out.print(x + " ");
            ArrayQueueModule.enqueue(x);
        }
        System.out.println("\nQueue size: " + ArrayQueueModule.size() + "\n");

        System.out.print("Extract all elements from Queue: ");
        while (!ArrayQueueModule.isEmpty()) {
            System.out.print(ArrayQueueModule.dequeue() + " ");
        }
        System.out.println("\nQueue size: " + ArrayQueueModule.size() + "\n");

        System.out.print("Add to Queue elements: ");
        for (int x = 1; x <= 10; x++) {
            System.out.print(x + " ");
            ArrayQueueModule.enqueue(x);
        }
        System.out.println("\nQueue size: " + ArrayQueueModule.size() + "\n");

        System.out.println("Clear Queue");
        ArrayQueueModule.clear();
        System.out.println("Queue size: " + ArrayQueueModule.size() + "\n");
    }

    private static void showArrayQueueADT() {
        System.out.println("=============");
        System.out.println("ArrayQueueADT");
        System.out.println("=============\n");

        ArrayQueueADT queue1 = new ArrayQueueADT();
        ArrayQueueADT queue2 = new ArrayQueueADT();

        System.out.print("Add to Queue_1 elements: ");
        for (int x = 1; x <= 10; x++) {
            System.out.print(x + " ");
            ArrayQueueADT.enqueue(queue1, x);
        }
        System.out.println("\nQueue_1 size: " + ArrayQueueADT.size(queue1) + "\n");

        System.out.print("Add to Queue_2 elements: ");
        for (int x = 12; x <= 30; x++) {
            System.out.print(x + " ");
            ArrayQueueADT.enqueue(queue2, x);
        }
        System.out.println("\nQueue_2 size: " + ArrayQueueADT.size(queue2) + "\n");

        System.out.print("Extract all elements from Queue_1: ");
        while (!ArrayQueueADT.isEmpty(queue1)) {
            System.out.print(ArrayQueueADT.dequeue(queue1) + " ");
        }
        System.out.println("\nQueue_1 size: " + ArrayQueueADT.size(queue1) + "\n");

        System.out.print("Extract all elements from Queue_2: ");
        while (!ArrayQueueADT.isEmpty(queue2)) {
            System.out.print(ArrayQueueADT.dequeue(queue2) + " ");
        }
        System.out.println("\nQueue_2 size: " + ArrayQueueADT.size(queue2) + "\n");

        System.out.print("Add to Queue_1 elements: ");
        for (int x = 1; x <= 10; x++) {
            System.out.print(-x + " ");
            ArrayQueueADT.enqueue(queue1, -x);
        }
        System.out.println("\nQueue_1 size: " + ArrayQueueADT.size(queue1) + "\n");

        System.out.print("Add to Queue_2 elements: ");
        for (int x = 12; x <= 30; x++) {
            System.out.print(-x + " ");
            ArrayQueueADT.enqueue(queue2, -x);
        }
        System.out.println("\nQueue_2 size: " + ArrayQueueADT.size(queue2) + "\n");

        System.out.println("Clear Queue_1");
        ArrayQueueADT.clear(queue1);
        System.out.println("Queue_1 size: " + ArrayQueueADT.size(queue1) + "\n");

        System.out.println("Clear Queue_2");
        ArrayQueueADT.clear(queue2);
        System.out.println("Queue_2 size: " + ArrayQueueADT.size(queue2) + "\n");
    }

    private static void showArrayQueue() {
        System.out.println("==========");
        System.out.println("ArrayQueue");
        System.out.println("==========\n");

        ArrayQueue queue1 = new ArrayQueue();
        ArrayQueue queue2 = new ArrayQueue();

        System.out.print("Add to Queue_1 elements: ");
        for (int x = 1; x <= 10; x++) {
            System.out.print(x + " ");
            queue1.enqueue(x);
        }
        System.out.println("\nQueue_1 size: " + queue1.size() + "\n");

        System.out.print("Add to Queue_2 elements: ");
        for (int x = 12; x <= 30; x++) {
            System.out.print(x + " ");
            queue2.enqueue(x);
        }
        System.out.println("\nQueue_2 size: " + queue2.size() + "\n");

        System.out.print("Extract all elements from Queue_1: ");
        while (!queue1.isEmpty()) {
            System.out.print(queue1.dequeue() + " ");
        }
        System.out.println("\nQueue_1 size: " + queue1.size() + "\n");

        System.out.print("Extract all elements from Queue_2: ");
        while (!queue2.isEmpty()) {
            System.out.print(queue2.dequeue() + " ");
        }
        System.out.println("\nQueue_2 size: " + queue2.size() + "\n");

        System.out.print("Add to Queue_1 elements: ");
        for (int x = 1; x <= 10; x++) {
            System.out.print(-x + " ");
            queue1.enqueue(-x);
        }
        System.out.println("\nQueue_1 size: " + queue1.size() + "\n");

        System.out.print("Add to Queue_2 elements: ");
        for (int x = 12; x <= 30; x++) {
            System.out.print(-x + " ");
            queue2.enqueue(-x);
        }
        System.out.println("\nQueue_2 size: " + queue2.size() + "\n");

        System.out.println("Clear Queue_1");
        queue1.clear();
        System.out.println("Queue_1 size: " + queue1.size() + "\n");

        System.out.println("Clear Queue_2");
        queue2.clear();
        System.out.println("Queue_2 size: " + queue2.size() + "\n");
    }

    public static void main(String[] args) {
        showArrayQueueModule();

        showArrayQueueADT();

        showArrayQueue();
    }
}
