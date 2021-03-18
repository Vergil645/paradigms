package expression;

import expression.calculator.Calculator;

public abstract class BinaryOperation<T> implements TripleExpression<T> {
    protected final TripleExpression<T> first, second;

    protected BinaryOperation(TripleExpression<T> first, TripleExpression<T> second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public T evaluate(Calculator<T> calc, int x, int y, int z) {
        return calculate(calc, first.evaluate(calc, x, y, z), second.evaluate(calc, x, y, z));
    }

    protected T calculate(Calculator<T> calc, T x, T y) {
        throw new UnsupportedOperationException();
    }
}
