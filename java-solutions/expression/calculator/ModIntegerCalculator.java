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
    public Integer valueOf(int arg) {
        return (arg % p + p) % p;
    }

    @Override
    public Integer add(Integer arg1, Integer arg2) {
        return (valueOf(arg1) + valueOf(arg2)) % p;
    }

    @Override
    public Integer subtract(Integer arg1, Integer arg2) {
        return (valueOf(arg1) - valueOf(arg2) + p) % p;
    }

    @Override
    public Integer multiply(Integer arg1, Integer arg2) {
        return (valueOf(arg1) * valueOf(arg2)) % p;
    }

    @Override
    public Integer divide(Integer arg1, Integer arg2) {
        if (arg2 == 0) {
            throw new DivisionByZeroException(String.format("Division by zero: %d / %d", arg1, arg2));
        }
        return (valueOf(arg1) * rev[valueOf(arg2)]) % p;
    }

    @Override
    public Integer negate(Integer arg) {
        return (-valueOf(arg) + p) % p;
    }

    @Override
    public Integer abs(Integer arg) {
        return valueOf(arg);
    }

    @Override
    public Integer square(Integer arg) {
        return (arg * arg) % p;
    }

    @Override
    public Integer mod(Integer arg1, Integer arg2) {
        return (arg1 % arg2) % p;
    }
}
