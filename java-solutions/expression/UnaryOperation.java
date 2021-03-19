package expression;

import expression.calculator.Calculator;

public abstract class UnaryOperation<T> implements CommonExpression<T> {
    protected final CommonExpression<T> arg;

    protected UnaryOperation(CommonExpression<T> arg) {
        this.arg = arg;
    }

    @Override
    public T evaluate(Calculator<T> calc, int x, int y, int z) {
        return calculate(calc, arg.evaluate(calc, x, y, z));
    }

    protected T calculate(Calculator<T> calc, T x) {
        throw new UnsupportedOperationException();
    }
}
