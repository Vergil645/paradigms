package search;

public class BinarySearchMax {
    // Pred: a != null && a -- array, which obtained by assigning a sorted (strictly) descending array
    //       to the end of an array sorted (strictly) ascending && a.length > 0
    // Post: R == max{a[i]} for i = 0, ..., a.length - 1
    private static int iterativeSearchMax(int[] a) {
        // Q: a != null && a -- array, which obtained by assigning a sorted (strictly) descending array
        //    to the end of an array sorted (strictly) ascending && a.length > 0
        int l = -1;
        // Q && l == -1
        int r = a.length - 1;
        // Q && l == -1 && r == a.length - 1
        // => l < r
        // Inv: l < r && l >= -1 && r < a.length
        //      && max{a[i]} for i = 0, ..., a.length - 1 == max{a[i]} for i = l + 1, ..., r
        while (r - l > 1) {
            // Inv && r - l > 1
            int m = (l + r) / 2;
            // Inv && r - l > 1 && m == (l + r) / 2;
            // => l < m < r
            if (a[m + 1] > a[m]) {
                // Inv && r - l > 1 && m == (l + r) / 2 && a[m + 1] > a[m]
                // => max{a[i]} for i = l + 1, ..., r == max{a[i]} for i = m + 1, ..., r
                l = m;
                // Inv
            } else {
                // Inv && r - l > 1 && m == (l + r) / 2 && a[m + 1] < a[m]
                // => max{a[i]} for i = l + 1, ..., r == max{a[i]} for i = l + 1, ..., m
                r = m;
                // Inv
            }
            // Inv
        }
        // Inv && r - l == 1
        // => a[r] == max{a[i]} for i = 0, ..., a.length - 1
        return a[r];
        // R == max{a[i]} for i = 0, ..., a.length - 1
    }

    // Pred: a != null && a -- array, which obtained by assigning a sorted (strictly) descending array
    //       to the end of an array sorted (strictly) ascending && a.length > 0
    //       && l < r && l >= -1 && r < a.length
    // Post: R == max{a[i]} for i = l + 1, ..., r
    private static int recursiveSearchMax(int[] a, int l, int r) {
        // Q: a != null && a -- array, which obtained by assigning a sorted (strictly) descending array
        //    to the end of an array sorted (strictly) ascending && a.length > 0
        //    && l < r && l >= -1 && r < a.length
        if (r - l == 1) {
            // Q && r - l == 1
            // => a[r] == max{a[i]} for i = l + 1, ..., r
            return a[r];
            // R == max{a[i]} for i = l + 1, ..., r
        }
        // Q && r - l > 1
        int m = (l + r) / 2;
        // Q && r - l > 1 && m == (l + r) / 2;
        // => l < m < r
        if (a[m + 1] > a[m]) {
            // Q && r - l > 1 && m == (l + r) / 2 && a[m + 1] > a[m]
            // => max{a[i]} for i = l + 1, ..., r == max{a[i]} for i = m + 1, ..., r
            return recursiveSearchMax(a, m, r);
            // R == max{a[i]} for i = l + 1, ..., r
        } else {
            // Q && r - l > 1 && m == (l + r) / 2 && a[m + 1] < a[m]
            // => max{a[i]} for i = l + 1, ..., r == max{a[i]} for i = l + 1, ..., m
            return recursiveSearchMax(a, l, m);
            // R == max{a[i]} for i = l + 1, ..., r
        }
        // R == max{a[i]} for i = l + 1, ..., r
    }

    // Pred: a != null && a -- array, which obtained by assigning a sorted (strictly) descending array
    //       to the end of an array sorted (strictly) ascending && a.length > 0
    // Post: R == max{a[i]} for i = 0, ..., a.length - 1
    private static int recursiveSearchMax(int[] a) {
        // a != null && a -- array, which obtained by assigning a sorted (strictly) descending array
        // to the end of an array sorted (strictly) ascending && a.length > 0
        return recursiveSearchMax(a, -1, a.length - 1);
        // R == max{a[i]} for i = 0, ..., a.length - 1
    }

    // Pred: args != null && args -- array of string representation of int values, which obtained by assigning
    //       a sorted (strictly) descending array to the end of an array sorted (strictly) ascending && args.length > 0
    // Post: print in console max{(int) args[i]} for i = 0, ..., args.length - 1
    public static void main(String[] args) {
        // Q: args != null && args -- array of string representation of int values, which obtained by assigning
        //    a sorted (strictly) descending array to the end of an array sorted (strictly) ascending
        //    && a.length > 0
        int[] a = new int[args.length];
        // Q && a == int[args.length]
        int i = 0;
        // Q && a == int[args.length] && i == 0
        // Inv: Q && a == int[args.length] && i >= 0 && i <= a.length
        //      && forall j = 0, ..., i - 1 : a[j] == (int) args[j]
        while (i < a.length) {
            // Inv && i < a.length
            a[i] = Integer.parseInt(args[i]);
            // Inv && i < a.length && a[i] == (int) args[i]
            i++;
            // Inv
        }
        // Inv && i == a.length
        assert iterativeSearchMax(a) == recursiveSearchMax(a);
        System.out.println(recursiveSearchMax(a));
        // print in console max{(int) args[i]} for i = 0, ..., args.length - 1
    }
}
