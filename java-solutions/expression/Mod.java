package expression;

import expression.calculator.Calculator;

public class Mod<T> extends BinaryOperation<T> {
    public Mod(Calculator<T> calc, CommonExpression<T> first, CommonExpression<T> second) {
        super(calc, first, second);
    }

    @Override
    protected T calculate(T x, T y) {
        return calc.mod(x, y);
    }
}
