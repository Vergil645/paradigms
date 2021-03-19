package expression;

import expression.calculator.Calculator;

public abstract class UnaryOperation<T> implements CommonExpression<T> {
    protected final Calculator<T> calc;
    protected final CommonExpression<T> arg;

    protected UnaryOperation(Calculator<T> calc, CommonExpression<T> arg) {
        this.calc = calc;
        this.arg = arg;
    }

    @Override
    public T evaluate(int x, int y, int z) {
        return calculate(arg.evaluate(x, y, z));
    }

    protected T calculate(T x) {
        throw new UnsupportedOperationException();
    }
}
