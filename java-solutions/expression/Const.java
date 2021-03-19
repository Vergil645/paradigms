package expression;

import expression.calculator.Calculator;

public class Const<T> implements CommonExpression<T> {
    private final T value;

    public Const(T value) {
        this.value = value;
    }

    @Override
    public T evaluate(Calculator<T> calc, int x, int y, int z) {
        return value;
    }
}
