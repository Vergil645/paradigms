package expression.calculator;

import expression.exceptions.DivisionByZeroException;

public class ModIntegerCalculator extends UncheckedIntegerCalculator {
    private static final int p = 1009;
    private static final int[] rev = new int[p];

    public ModIntegerCalculator() {
        rev[1] = 1;
        for (int i = 2; i < p; i++)
            rev[i] = (p - (p / i) * rev[p % i] % p) % p;
    }

    @Override
    public Integer valueOf(int x) {
        return (x % p + p) % p;
    }

    @Override
    public Integer add(Integer x, Integer y) {
        return (valueOf(x) + valueOf(y)) % p;
    }

    @Override
    public Integer subtract(Integer x, Integer y) {
        return (valueOf(x) - valueOf(y) + p) % p;
    }

    @Override
    public Integer multiply(Integer x, Integer y) {
        return (valueOf(x) * valueOf(y)) % p;
    }

    @Override
    public Integer divide(Integer x, Integer y) {
        if (y == 0) {
            throw new DivisionByZeroException(String.format("Division by zero: %d / %d", x, y));
        }
        return (valueOf(x) * rev[valueOf(y)]) % p;
    }

    @Override
    public Integer negate(Integer x) {
        return (-valueOf(x) + p) % p;
    }

    @Override
    public Integer abs(Integer x) {
        return valueOf(x);
    }

    @Override
    public Integer square(Integer x) {
        return (x * x) % p;
    }

    @Override
    public Integer mod(Integer x, Integer y) {
        return (x % y) % p;
    }
}
