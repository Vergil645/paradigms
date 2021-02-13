package search;

public class BinarySearch {
    // Pred: a -- отсортирован по невозрастанию
    // Post: return минимальное значение индекса i такое, что a[i] <= x
    //       (считаем, что a[a.length] <= x)
    private static int iterativeSearch(int x, int[] a) {
        // a -- отсортирован по невозрастанию
        int l = -1;
        // a -- отсортирован по невозрастанию && l == -1
        int r = a.length;
        // a -- отсортирован по невозрастанию && l == -1 && r == a.length
        // => r - l >= 1 && a[l] > x && a[r] <= x (считаем, что a[a.length] <= x и a[-1] > x)

        // Inv: a -- отсортирован по невозрастанию && r - l >= 1 && a[l] > x && a[r] <= x
        while (r - l > 1) {
            // Inv && r - l > 1
            int m = (l + r) / 2;
            // Inv && r - l > 1 && m == (l + r) / 2
            // => l < m && m < r
            if (a[m] <= x) {
                // Inv && r - l > 1 && l < m && m < r && a[m] <= x
                r = m;
                // Inv (т.к. l < m => r - l >= 1) && r - l уменьшилось (т.к. l < m < r)
            } else {
                // Inv && r - l > 1 && l < m && m < r && a[m] > x
                l = m;
                // Inv (т.к. m < r => r - l >= 1) && r - l уменьшилось (т.к. l < m < r)
            }
            // Inv && r - l уменьшилось

        }
        // Inv
        // => a -- отсортирован по невозрастанию && r - l == 1 && a[l] > x && a[r] <= x
        // => r == минимальное значение индекса i такое, что a[i] <= x
        return r;
        // return минимальное значение индекса i такое, что a[i] <= x
    }

    // Pred: a -- отсортирован по невозрастанию && l < r && a[l] > x && a[r] <= x
    // Post: return минимальное значение индекса l < i <= r такое, что a[i] <= x
    //       (считаем, что a[a.length] <= x)
    private static int recursiveSearch(int x, int[] a, int l, int r) {
        // Inv: a -- отсортирован по невозрастанию && l < r && a[l] > x && a[r] <= x
        if (r - l == 1) {
            // Inv && l + 1 == r
            // => r == минимальное значение индекса i такое, что a[i] <= x
            return r;
            // return минимальное значение индекса i такое, что a[i] <= x
        } else {
            // Inv && r - l > 1
            int m = (l + r) / 2;
            // Inv && r - l > 1 && m == (l + r) / 2
            // => l < m && m < r
            if (a[m] <= x) {
                // Inv && r - l > 1 && l < m && m < r && a[m] <= x
                // =>  a -- отсортирован по невозрастанию && l < m && a[l] > x && a[m] <= x (Pred)
                //     && r - l > m - l >= 1 (диапазон уменьшился)
                return recursiveSearch(x, a, l, m);
                // Inv && r - l > 1 && l < m && m < r && a[m] <= x
                //     && return минимальное значение индекса l < i <= m такое, что a[i] <= x
                // =>  return минимальное значение индекса l < i <= r такое, что a[i] <= x (т.к. m < r)
            } else {
                // Inv && r - l > 1 && l < m && m < r && a[m] > x
                // =>  a -- отсортирован по невозрастанию && m < r && a[m] > x && a[r] <= x (Pred)
                //     && r - l > r - m >= 1 (диапазон уменьшился)
                return recursiveSearch(x, a, m, r);
                // Inv && r - l > 1 && l < m && m < r && a[m] > x
                //     && return минимальное значение индекса m < i <= r такое, что a[i] <= x
                // =>  return минимальное значение индекса l < i <= r такое, что a[i] <= x (т.к. l < m)
            }
        }
        // return минимальное значение индекса l < i <= r такое, что a[i] <= x
    }

    // Pred: a -- отсортирован по невозрастанию
    // Post: return минимальное значение индекса i такое, что a[i] <= x
    //       (считаем, что a[a.length] <= x)
    private static int recursiveSearch(int x, int[] a) {
        // a -- отсортирован по невозрастанию && -1 < a.length && a[-1] > x && a[a.length] <= x
        //      (считаем, что a[a.length] <= x и a[-1] > x)
        return recursiveSearch(x, a, -1, a.length);
        // return минимальное значение индекса -1 < i <= a.length такое, что a[i] <= x
        // => return минимальное значение индекса i такое, что a[i] <= x
    }

    // Pred: args -- массив строковых представлений целых чисел (int)
    //       && (int) args[1], ..., (int) args[args.length - 1] -- отсортированы по невозрастанию
    // Post: выводим в консоль минимальное значение индекса i такое, что (int) args[i + 1] <= (int) args[0]
    //       (считаем, что a[a.length] <= x)
    public static void main(String[] args) {
        // Q: args -- массив строковых представлений целых чисел (int)
        //    && (int) args[1], ..., (int) args[args.length - 1] -- отсортированы по невозрастанию
        int x = Integer.parseInt(args[0]);
        // Q && x == (int) args[0]
        int[] a = new int[args.length - 1];
        // Q && x == (int) args[0] && a == int[args.length - 1]
        int i = 0;
        // Inv && i == 0
        // Inv: Q && x == (int) args[0] && a == int[args.length - 1]
        //      && i <= a.length
        //      && [ a[0], ..., a[i - 1] ] == [ (int) args[1], ..., (int) args[i] ]
        // (считаем, что для i == 0 Inv == true)
        while (i < a.length) {
            // Inv && i < a.length
            // => i < args.length - 1 => i + 1 < args.length
            a[i] = Integer.parseInt(args[i + 1]);
            // Inv && i < a.length && a[i] == (int) args[i + 1]
            i++;
            // Inv
        }
        // Inv && i == a.length
        // => x == (int) args[0] && для любого 0 <= id < a.length : a[id] = (int) args[id + 1]
        //    && a -- отсортирован по невозрастанию
        System.out.println(iterativeSearch(x, a));
        // Inv && вывели в консоль минимальное значение индекса i такое, что a[i] <= x
        // => вывели в консоль минимальное значение индекса i такое, что (int) args[i + 1] <= (int) args[0]
    }
}
