package expression;

import expression.calculator.Calculator;

public class Subtract<T> extends BinaryOperation<T> {
    public Subtract(CommonExpression<T> first, CommonExpression<T> second) {
        super(first, second);
    }

    @Override
    protected T calculate(Calculator<T> calc, T x, T y) {
        return calc.subtract(x, y);
    }

    @Override
    protected String getOperator() {
        return "-";
    }

    @Override
    public int getRank() {
        return 1234;
    }

    @Override
    protected boolean getAssociativity() {
        return false;
    }

    @Override
    protected boolean getContinuity() {
        return true;
    }
}
