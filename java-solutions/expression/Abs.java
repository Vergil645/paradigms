package expression;

import expression.calculator.Calculator;

public class Abs<T> extends UnaryOperation<T> {
    public Abs(TripleExpression<T> arg) {
        super(arg);
    }

    @Override
    protected T calculate(Calculator<T> calc, T x) {
        return calc.abs(x);
    }
}
