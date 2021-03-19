package expression.calculator;

import expression.Const;
import expression.CommonExpression;
import expression.exceptions.ConstantFormatException;

public class DoubleCalculator implements Calculator<Double> {
    @Override
    public Double valueOf(int x) {
        return (double) x;
    }

    @Override
    public Double add(Double x, Double y) {
        return x + y;
    }

    @Override
    public Double subtract(Double x, Double y) {
        return x - y;
    }

    @Override
    public Double multiply(Double x, Double y) {
        return x * y;
    }

    @Override
    public Double divide(Double x, Double y) {
        return x / y;
    }

    @Override
    public Double negate(Double x) {
        return -x;
    }

    @Override
    public Double abs(Double x) {
        return x >= 0 ? x : -x;
    }

    @Override
    public Double square(Double x) {
        return x * x;
    }

    @Override
    public Double mod (Double x, Double y) {
        return x % y;
    }

    @Override
    public boolean isValidSymbol(char elem) {
        return elem == '.' || ('0' <= elem && elem <= '9');
    }

    @Override
    public CommonExpression<Double> parseConst(String str) throws ConstantFormatException {
        try {
            return new Const<>(Double.parseDouble(str));
        } catch (NumberFormatException e) {
            throw new ConstantFormatException("Invalid constant: " + str);
        }
    }
}
