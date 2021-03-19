package expression.calculator;

import expression.exceptions.*;

public class IntegerCalculator extends UncheckedIntegerCalculator {
    @Override
    public Integer add(Integer arg1, Integer arg2) {
        if (arg2 > 0 && arg1 > Integer.MAX_VALUE - arg2){
            throw new OverflowException(String.format("Overflow: %d + %d is greater than Integer.MAX_VALUE", arg1, arg2));
        } else if (arg2 < 0 && arg1 < Integer.MIN_VALUE - arg2) {
            throw new OverflowException(String.format("Overflow: %d + %d is less than Integer.MIN_VALUE", arg1, arg2));
        }
        return arg1 + arg2;
    }

    @Override
    public Integer subtract(Integer arg1, Integer arg2) {
        if (arg2 < 0 && arg1 > Integer.MAX_VALUE + arg2){
            throw new OverflowException(String.format("Overflow: %d - %d is greater than Integer.MAX_VALUE", arg1, arg2));
        } else if (arg2 > 0 && arg1 < Integer.MIN_VALUE + arg2){
            throw new OverflowException(String.format("Overflow: %d - %d is less than Integer.MIN_VALUE", arg1, arg2));
        }
        return arg1 - arg2;
    }

    @Override
    public Integer multiply(Integer arg1, Integer arg2) {
        if (arg1 > arg2) {
            int tmp = arg2;
            arg2 = arg1;
            arg1 = tmp;
        }
        if ((arg2 > 0 && arg1 > Integer.MAX_VALUE / arg2) || (arg2 < 0 && arg1 < Integer.MAX_VALUE / arg2)) {
            throw new OverflowException(String.format("Overflow: %d * %d is greater than Integer.MAX_VALUE", arg1, arg2));
        } else if ((arg2 > 0 && arg1 < Integer.MIN_VALUE / arg2) || (arg2 < 0 && -arg1 < Integer.MIN_VALUE / (-arg2))) {
            throw new OverflowException(String.format("Overflow: %d * %d is less than Integer.MIN_VALUE", arg1, arg2));
        }
        return arg1 * arg2;
    }

    @Override
    public Integer divide(Integer arg1, Integer arg2) {
        if (arg2 == 0) {
            throw new DivisionByZeroException(String.format("Division by zero: %d / %d", arg1, arg2));
        } else if (arg1 == Integer.MIN_VALUE && arg2 == -1) {
            throw new OverflowException(String.format("Overflow: %d / %d is greater than Integer.MAX_VALUE", arg1, arg2));
        }
        return arg1 / arg2;
    }

    @Override
    public Integer negate(Integer arg) {
        if (arg == Integer.MIN_VALUE) {
            throw new OverflowException(String.format("Overflow: -(%d) is greater than Integer.MAX_VALUE", arg));
        }
        return -arg;
    }

    @Override
    public Integer abs(Integer arg) {
        if (arg == Integer.MIN_VALUE) {
            throw new OverflowException(String.format("Overflow: abs(%d) is greater than Integer.MAX_VALUE", arg));
        }
        return arg >= 0 ? arg : -arg;
    }

    @Override
    public Integer square(Integer arg) {
        try {
            return multiply(arg, arg);
        } catch (OverflowException e) {
            throw new OverflowException(String.format("Overflow: (%d)^2 is greater than Integer.MAX_VALUE", arg));
        }
    }
}
