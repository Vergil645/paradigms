package expression;

import expression.calculator.Calculator;

public class Add<T> extends BinaryOperation<T> {
    public Add(CommonExpression<T> first, CommonExpression<T> second) {
        super(first, second);
    }

    @Override
    protected T calculate(Calculator<T> calc, T x, T y) {
        return calc.add(x, y);
    }
}
