package expression;

import expression.calculator.Calculator;

public class Mod<T> extends BinaryOperation<T> {
    public Mod(CommonExpression<T> first, CommonExpression<T> second) {
        super(first, second);
    }

    @Override
    protected T calculate(Calculator<T> calc, T x, T y) {
        return calc.mod(x, y);
    }
}
