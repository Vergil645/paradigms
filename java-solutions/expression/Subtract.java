package expression;

import expression.calculator.Calculator;

public class Subtract<T> extends BinaryOperation<T> {
    public Subtract(Calculator<T> calc, CommonExpression<T> first, CommonExpression<T> second) {
        super(calc, first, second);
    }

    @Override
    protected T calculate(T x, T y) {
        return calc.subtract(x, y);
    }
}
