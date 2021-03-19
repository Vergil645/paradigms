package expression;

import expression.calculator.Calculator;

public class Multiply<T> extends BinaryOperation<T> {
    public Multiply(Calculator<T> calc, CommonExpression<T> first, CommonExpression<T> second) {
        super(calc, first, second);
    }

    @Override
    protected T calculate(T x, T y) {
        return calc.multiply(x, y);
    }
}
