package expression;

import expression.calculator.Calculator;

public class Negate<T> extends UnaryOperation<T> {
    public Negate(Calculator<T> calc, CommonExpression<T> arg) {
        super(calc, arg);
    }

    @Override
    protected T calculate(T x) {
        return calc.negate(x);
    }
}
