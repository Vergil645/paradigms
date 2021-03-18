package expression;

import expression.calculator.Calculator;

public interface TripleExpression<T> extends ToMiniString {
    T evaluate(Calculator<T> calc, int x, int y, int z);
}
