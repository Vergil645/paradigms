package search;

public class BinarySearch {
    // Pred: a -- отсортирован по невозрастанию && a.length >= 0
    // Post: R == минимальное значение индекса i такое, что a[i] <= x
    //       (если все a[i] > x, то R == a.length)
    private static int iterativeSearch(int x, int[] a) {
        // a -- отсортирован по невозрастанию && a.length >= 0
        int l = -1;
        // a -- отсортирован по невозрастанию && a.length >= 0 && l == -1
        int r = a.length;
        // a -- отсортирован по невозрастанию && a.length >= 0 && l == -1 && r == a.length
        // => r - l >= 1 && a[l] > x && a[r] <= x (считаем, что a[a.length] <= x и a[-1] > x)

        // Inv: a -- отсортирован по невозрастанию && a.length >= 0 && r - l >= 1 && a[l] > x && a[r] <= x
        while (r - l > 1) {
            // Inv && r - l > 1
            int m = (l + r) / 2;
            // Inv && r - l > 1 && m == (l + r) / 2
            // => l < m && m < r
            if (a[m] <= x) {
                // Inv && r - l > 1 && l < m && m < r && a[m] <= x
                r = m;
                // Inv (l < m => r - l >= 1) && r - l уменьшилось (т.к. l < m < r)
            } else {
                // Inv jhgk&& r - l > 1 && l < m && m < r && a[m] > x
                l = m;
                // Inv (m < r => r - l >= 1) && r - l уменьшилось (т.к. l < m < r)
            }

        }
        // a -- отсортирован по невозрастанию && a.length >= 0 && r - l == 1 && a[l] > x && a[r] <= x
        // => r == минимальное значение индекса i такое, что a[i] <= x
        return r;
    }

    // Pred: a -- отсортирован по невозрастанию && a.length >= 0
    // Post: R == минимальное значение индекса i такое, что a[i] <= x
    //       (если все a[i] > x, то R == a.length)
    private static int recursiveSearch(int x, int[] a) {
        return recursiveSearch(x, a, -1, a.length);
    }


    // Pred: a -- отсортирован по невозрастанию && a.length >= 0 !!!!!!!!!!!!!!!!!!!!!!!
    // Post: R == минимальное значение индекса i такое, что a[i] <= x
    //       (если все a[i] > x, то R == a.length)
    private static int recursiveSearch(int x, int[] a, int l, int r) {
        if (r - l <= 1) {

            return r;
        } else {

            int m = (l + r) / 2;

            if (a[m] <= x) {

                return recursiveSearch(x, a, l, m);
            } else {

                return recursiveSearch(x, a, m, r);
            }
        }
    }

    public static void main(String[] args) {

        int x = Integer.parseInt(args[0]);

        int[] a = new int[args.length - 1];
        for (int i = 1; i < args.length; i++) {
            a[i - 1] = Integer.parseInt(args[i]);
        }

        System.out.println(iterativeSearch(x, a));
    }
}
