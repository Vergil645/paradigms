package search;

public class BinarySearch {
    // Pred: a -- отсортирован по невозрастанию
    // Post: forall i = 0, ..., a.length - 1 : a[i] == a'[i]
    //       && R == минимальное значение индекса i такое, что a[i] <= x
    // (считаем, что a[-1] > x >= a[a.length])
    private static int iterativeSearch(int x, int[] a) {
        // Immutability: forall i = 0, ..., a.length - 1 : a[i] == a'[i]
        //               && a -- отсортирован по невозрастанию
        int l = -1;
        // Immutability && l == -1
        int r = a.length;
        // Immutability && l == -1 && r == a.length

        // Inv: Immutability && r - l >= 1 && a[l] > x && x >= a[r]
        while (r - l > 1) {
            // Inv && r - l > 1
            int m = (l + r) / 2;
            // Inv && r - l > 1 && m == (l + r) / 2
            // => l < m && m < r
            if (a[m] <= x) {
                // Inv && l < m && m < r && x >= a[m]
                r = m;
                // Inv && (r - l) -- уменьшилось
            } else {
                // Inv && l < m && m < r && a[m] > x
                l = m;
                // Inv && (r - l) -- уменьшилось
            }
            // Inv && (r - l) -- уменьшилось
        }
        // Inv && r - l == 1
        // => Immutability && r - l == 1 && a[l] > x && x >= a[r]
        // => r == минимальное значение индекса i такое, что a[i] <= x
        return r;
        // forall i = 0, ..., a.length - 1 : a[i] == a'[i]
        // && R == минимальное значение индекса i такое, что a[i] <= x
    }

    // Pred: a -- отсортирован по невозрастанию && l < r && a[l] > x && x >= a[r]
    // Post: forall i = 0, ..., a.length - 1 : a[i] == a'[i]
    //       && R == минимальное значение индекса l < i <= r такое, что a[i] <= x
    // (считаем, что a[-1] > x >= a[a.length])
    private static int recursiveSearch(int x, int[] a, int l, int r) {
        // Inv: a -- отсортирован по невозрастанию && forall i = 0...a.length-1 : a[i] == a'[i]
        //      && l < r && a[l] > x && x >= a[r]
        if (r - l == 1) {
            // Inv && r - l == 1
            // => r == минимальное значение индекса l < i <= r такое, что a[i] <= x
            return r;
            // forall i = 0, ..., a.length - 1 : a[i] == a'[i]
            // && R == минимальное значение индекса l < i <= r такое, что a[i] <= x
        } else {
            // Inv && r - l > 1
            int m = (l + r) / 2;
            // Inv && r - l > 1 && m == (l + r) / 2
            // => l < m && m < r
            if (a[m] <= x) {
                // Inv && l < m && m < r && x >= a[m]
                // =>  a -- отсортирован по невозрастанию && l < m && a[l] > x && x >= a[m]
                //     && r - l > m - l >= 1 (диапазон уменьшился)
                return recursiveSearch(x, a, l, m);
                // Inv && l < m && m < r && x >= a[m]
                //     && R == минимальное значение индекса l < i <= m такое, что a[i] <= x
                // =>  forall i = 0, ..., a.length - 1 : a[i] == a'[i]
                //     && R == минимальное значение индекса l < i <= r такое, что a[i] <= x
            } else {
                // Inv && l < m && m < r && a[m] > x
                // =>  a -- отсортирован по невозрастанию && m < r && a[m] > x && x >= a[r]
                //     && r - l > r - m >= 1 (диапазон уменьшился)
                return recursiveSearch(x, a, m, r);
                // Inv && l < m && m < r && a[m] > x
                //     && R == минимальное значение индекса m < i <= r такое, что a[i] <= x
                // =>  forall i = 0, ..., a.length - 1 : a[i] == a'[i]
                //     && R == минимальное значение индекса l < i <= r такое, что a[i] <= x
            }
        }
        // forall i = 0, ..., a.length - 1 : a[i] == a'[i]
        // && R == минимальное значение индекса l < i <= r такое, что a[i] <= x
    }

    // Pred: a -- отсортирован по невозрастанию
    // Post: forall i = 0, ..., a.length - 1 : a[i] == a'[i]
    //       && R == минимальное значение индекса i такое, что a[i] <= x
    // (считаем, что a[-1] > x >= a[a.length])
    private static int recursiveSearch(int x, int[] a) {
        // a -- отсортирован по невозрастанию && -1 < a.length && a[-1] > x && x >= a[a.length]
        return recursiveSearch(x, a, -1, a.length);
        // forall i = 0, ..., a.length - 1 : a[i] == a'[i]
        // && R == минимальное значение индекса -1 < i <= a.length такое, что a[i] <= x
        // => forall i = 0, ..., a.length - 1 : a[i] == a'[i]
        //    && R == минимальное значение индекса i такое, что a[i] <= x
    }

    // Pred: args -- массив строковых представлений целых чисел (int)
    //       && (int) args[1], ..., (int) args[args.length - 1] -- отсортированы по невозрастанию
    // Post: выводим в консоль минимальное значение индекса i такое, что (int) args[i + 1] <= (int) args[0]
    // (считаем, что (int) args[args.length] <= (int) args[0])
    public static void main(String[] args) {
        // Q: args -- массив строковых представлений целых чисел (int)
        //    && (int) args[1], ..., (int) args[args.length - 1] -- отсортированы по невозрастанию
        int x = Integer.parseInt(args[0]);
        // Q && x == (int) args[0]
        int[] a = new int[args.length - 1];
        // Q && x == (int) args[0] && a == int[args.length - 1]
        int i = 0;

        // Inv: Q && x == (int) args[0] && a == int[args.length - 1]
        //      && i <= a.length && forall j = 0, ..., i - 1 : a[j] == (int) args[j + 1]

        // Inv && i == 0
        while (i < a.length) {
            // Inv && i < a.length
            // => i + 1 < args.length
            a[i] = Integer.parseInt(args[i + 1]);
            // Inv && i < a.length && a[i] == (int) args[i + 1]
            i++;
            // Inv && i -- увеличилось
        }
        // Inv && i == a.length
        // => x == (int) args[0] && forall i = 0, ..., a.length : a[i] = (int) args[i + 1]
        //    && a -- отсортирован по невозрастанию
        assert iterativeSearch(x, a) == recursiveSearch(x, a);
        System.out.println(recursiveSearch(x, a));
        // Inv && i == a.length && вывели в консоль минимальное значение индекса i такое, что a[i] <= x
        // => вывели в консоль минимальное значение индекса i такое, что (int) args[i + 1] <= (int) args[0]
    }
}
