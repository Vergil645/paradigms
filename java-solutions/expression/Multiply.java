package expression;

import expression.calculator.Calculator;

public class Multiply<T> extends BinaryOperation<T> {
    public Multiply(CommonExpression<T> first, CommonExpression<T> second) {
        super(first, second);
    }

    @Override
    protected T calculate(Calculator<T> calc, T x, T y) {
        return calc.multiply(x, y);
    }

    @Override
    protected String getOperator() {
        return "*";
    }

    @Override
    public int getRank() {
        return 123456;
    }

    @Override
    protected boolean getAssociativity() {
        return true;
    }

    @Override
    protected boolean getContinuity() {
        return true;
    }
}
