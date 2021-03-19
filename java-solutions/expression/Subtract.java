package expression;

import expression.calculator.Calculator;

public class Subtract<T> extends BinaryOperation<T> {
    public Subtract(CommonExpression<T> first, CommonExpression<T> second) {
        super(first, second);
    }

    @Override
    protected T calculate(Calculator<T> calc, T x, T y) {
        return calc.subtract(x, y);
    }
}
