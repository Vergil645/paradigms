package expression;

import expression.calculator.Calculator;

public class Divide<T> extends BinaryOperation<T> {
    public Divide(Calculator<T> calc, CommonExpression<T> first, CommonExpression<T> second) {
        super(calc, first, second);
    }

    @Override
    protected T calculate(T x, T y) {
        return calc.divide(x, y);
    }
}
