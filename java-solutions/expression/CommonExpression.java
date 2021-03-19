package expression;

import expression.calculator.Calculator;

public interface CommonExpression<T> {
    T evaluate(Calculator<T> calc, int x, int y, int z);
}
