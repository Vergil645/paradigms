package expression;

import expression.calculator.Calculator;

public class Add<T> extends BinaryOperation<T> {
    public Add(Calculator<T> calc, CommonExpression<T> first, CommonExpression<T> second) {
        super(calc, first, second);
    }

    @Override
    protected T calculate(T x, T y) {
        return calc.add(x, y);
    }
}
