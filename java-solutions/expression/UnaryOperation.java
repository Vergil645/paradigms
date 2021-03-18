package expression;

import expression.calculator.Calculator;

public abstract class UnaryOperation<T> implements TripleExpression<T> {
    protected final TripleExpression<T> arg;

    protected UnaryOperation(TripleExpression<T> arg) {
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
