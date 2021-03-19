package expression;

import expression.calculator.Calculator;

public abstract class BinaryOperation<T> implements CommonExpression<T> {
    protected final Calculator<T> calc;
    protected final CommonExpression<T> first, second;

    protected BinaryOperation(Calculator<T> calc, CommonExpression<T> first, CommonExpression<T> second) {
        this.calc = calc;
        this.first = first;
        this.second = second;
    }

    @Override
    public T evaluate(int x, int y, int z) {
        return calculate(first.evaluate(x, y, z), second.evaluate(x, y, z));
    }

    protected T calculate(T x, T y) {
        throw new UnsupportedOperationException();
    }
}
