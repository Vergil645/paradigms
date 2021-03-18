package expression;

import expression.calculator.Calculator;

public class Multiply<T> extends BinaryOperation<T> {
    public Multiply(TripleExpression<T> first, TripleExpression<T> second) {
        super(first, second);
    }

    @Override
    protected T calculate(Calculator<T> calc, T x, T y) {
        return calc.multiply(x, y);
    }
}
