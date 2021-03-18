package expression;

import expression.calculator.Calculator;

public class Divide<T> extends BinaryOperation<T> {
    public Divide(TripleExpression<T> first, TripleExpression<T> second) {
        super(first, second);
    }

    @Override
    protected T calculate(Calculator<T> calc, T x, T y) {
        return calc.divide(x, y);
    }
}
