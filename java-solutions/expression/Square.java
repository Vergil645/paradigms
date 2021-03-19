package expression;

import expression.calculator.Calculator;

public class Square<T> extends UnaryOperation<T> {
    public Square(CommonExpression<T> arg) {
        super(arg);
    }

    @Override
    protected T calculate(Calculator<T> calc, T x) {
        return calc.square(x);
    }
}
