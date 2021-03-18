package expression;

import expression.calculator.Calculator;

import java.util.Objects;

public abstract class UnaryOperation<T> implements CommonExpression<T> {
    protected final CommonExpression<T> arg;

    protected UnaryOperation(CommonExpression<T> arg) {
        this.arg = arg;
    }

    @Override
    public T evaluate(Calculator<T> calc, int x) {
        return calculate(calc, arg.evaluate(calc, x));
    }

    @Override
    public T evaluate(Calculator<T> calc, int x, int y, int z) {
        return calculate(calc, arg.evaluate(calc, x, y, z));
    }

    protected T calculate(Calculator<T> calc, T x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        toString(sb);
        return sb.toString();
    }

    @Override
    public void toString(StringBuilder sb) {
        sb.append(getOperator()).append(" ");
        arg.toString(sb);
    }

    @Override
    public String toMiniString() {
        StringBuilder sb = new StringBuilder();
        toMiniString(sb, -1, false, false);
        return sb.toString();
    }

    @Override
    public void toMiniString(StringBuilder sb, int prevRank, boolean isLeft, boolean prevAssociativity) {
        sb.append(getOperator()).append(" ");
        arg.toMiniString(sb, getRank(), false, false);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        UnaryOperation<?> that = (UnaryOperation<?>) obj;
        return Objects.equals(arg, that.arg);
    }

    @Override
    public int hashCode() {
        return Objects.hash(arg);
    }

    @Override
    public int getRank() {
        return Integer.MAX_VALUE;
    }

    protected abstract String getOperator();
}
