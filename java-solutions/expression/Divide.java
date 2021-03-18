package expression;

import expression.calculator.Calculator;

public class Divide<T> extends BinaryOperation<T> {
    public Divide(CommonExpression<T> first, CommonExpression<T> second) {
        super(first, second);
    }

    @Override
    protected T calculate(Calculator<T> calc, T x, T y) {
        return calc.divide(x, y);
    }

    @Override
    protected String getOperator() {
        return "/";
    }

    @Override
    public int getRank() {
        return 123456;
    }

    @Override
    protected boolean getAssociativity() {
        return false;
    }

    @Override
    protected boolean getContinuity() {
        return false;
    }
}
