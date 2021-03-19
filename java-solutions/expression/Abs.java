package expression;

import expression.calculator.Calculator;

public class Abs<T> extends UnaryOperation<T> {
    public Abs(Calculator<T> calc, CommonExpression<T> arg) {
        super(calc, arg);
    }

    @Override
    protected T calculate(T x) {
        return calc.abs(x);
    }
}
