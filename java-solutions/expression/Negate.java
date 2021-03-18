package expression;

import expression.calculator.Calculator;

public class Negate<T> extends UnaryOperation<T> {
    public Negate(TripleExpression<T> arg) {
        super(arg);
    }

    @Override
    protected T calculate(Calculator<T> calc, T x) {
        return calc.negate(x);
    }
}
