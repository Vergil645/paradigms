package expression.calculator;

import expression.Const;
import expression.CommonExpression;
import expression.exceptions.ConstantFormatException;

public class UncheckedIntegerCalculator implements Calculator<Integer> {
    @Override
    public Integer valueOf(int x) {
        return x;
    }

    @Override
    public Integer add(Integer x, Integer y) {
        return x + y;
    }

    @Override
    public Integer subtract(Integer x, Integer y) {
        return x - y;
    }

    @Override
    public Integer multiply(Integer x, Integer y) {
        return x * y;
    }

    @Override
    public Integer divide(Integer x, Integer y) {
        return x / y;
    }

    @Override
    public Integer negate(Integer x) {
        return -x;
    }

    @Override
    public Integer abs(Integer x) {
        return x >= 0 ? x : -x;
    }

    @Override
    public Integer square(Integer x) {
        return x * x;
    }

    @Override
    public Integer mod(Integer x, Integer y) {
        return x % y;
    }

    @Override
    public boolean isValidSymbol(char elem) {
        return '0' <= elem && elem <= '9';
    }

    @Override
    public CommonExpression<Integer> parseConst(String str) throws ConstantFormatException {
        try {
            return new Const<>(Integer.parseInt(str));
        } catch (NumberFormatException e) {
            throw new ConstantFormatException("Invalid constant: " + str);
        }
    }
}
