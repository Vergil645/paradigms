package expression;

import expression.calculator.Calculator;

public class Square<T> extends UnaryOperation<T> {
    public Square(Calculator<T> calc, CommonExpression<T> arg) {
        super(calc, arg);
    }

    @Override
    protected T calculate(T x) {
        return calc.square(x);
    }
}
