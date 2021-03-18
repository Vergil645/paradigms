package expression;

import expression.calculator.Calculator;

public interface Expression<T> extends ToMiniString {
    T evaluate(Calculator<T> calc, int x);
}
