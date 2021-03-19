package expression.calculator;

import expression.exceptions.*;

public class IntegerCalculator extends UncheckedIntegerCalculator {
    @Override
    public Integer valueOf(int x) {
        return x;
    }

    @Override
    public Integer add(Integer x, Integer y) {
        if (y > 0 && x > Integer.MAX_VALUE - y){
            throw new OverflowException(String.format("Overflow: %d + %d is greater than Integer.MAX_VALUE", x, y));
        } else if (y < 0 && x < Integer.MIN_VALUE - y) {
            throw new OverflowException(String.format("Overflow: %d + %d is less than Integer.MIN_VALUE", x, y));
        }
        return x + y;
    }

    @Override
    public Integer subtract(Integer x, Integer y) {
        if (y < 0 && x > Integer.MAX_VALUE + y){
            throw new OverflowException(String.format("Overflow: %d - %d is greater than Integer.MAX_VALUE", x, y));
        } else if (y > 0 && x < Integer.MIN_VALUE + y){
            throw new OverflowException(String.format("Overflow: %d - %d is less than Integer.MIN_VALUE", x, y));
        }
        return x - y;
    }

    @Override
    public Integer multiply(Integer x, Integer y) {
        if (x > y) {
            int tmp = y;
            y = x;
            x = tmp;
        }
        if ((y > 0 && x > Integer.MAX_VALUE / y) || (y < 0 && x < Integer.MAX_VALUE / y)) {
            throw new OverflowException(String.format("Overflow: %d * %d is greater than Integer.MAX_VALUE", x, y));
        } else if ((y > 0 && x < Integer.MIN_VALUE / y) || (y < 0 && -x < Integer.MIN_VALUE / (-y))) {
            throw new OverflowException(String.format("Overflow: %d * %d is less than Integer.MIN_VALUE", x, y));
        }
        return x * y;
    }

    @Override
    public Integer divide(Integer x, Integer y) {
        if (y == 0) {
            throw new DivisionByZeroException(String.format("Division by zero: %d / %d", x, y));
        } else if (x == Integer.MIN_VALUE && y == -1) {
            throw new OverflowException(String.format("Overflow: %d / %d is greater than Integer.MAX_VALUE", x, y));
        }
        return x / y;
    }

    @Override
    public Integer negate(Integer x) {
        if (x == Integer.MIN_VALUE) {
            throw new OverflowException(String.format("Overflow: -(%d) is greater than Integer.MAX_VALUE", x));
        }
        return -x;
    }

    @Override
    public Integer abs(Integer x) {
        if (x == Integer.MIN_VALUE) {
            throw new OverflowException(String.format("Overflow: abs(%d) is greater than Integer.MAX_VALUE", x));
        }
        return x >= 0 ? x : -x;
    }

    @Override
    public Integer square(Integer x) {
        try {
            return multiply(x, x);
        } catch (OverflowException e) {
            throw new OverflowException(String.format("Overflow: (%d)^2 is greater than Integer.MAX_VALUE", x));
        }
    }

    @Override
    public Integer mod (Integer x, Integer y) {
        return x % y;
    }
}
