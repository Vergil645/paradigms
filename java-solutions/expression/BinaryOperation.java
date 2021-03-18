package expression;

import expression.calculator.Calculator;

import java.util.Objects;

public abstract class BinaryOperation<T> implements CommonExpression<T> {
    protected final CommonExpression<T> first, second;

    protected BinaryOperation(CommonExpression<T> first, CommonExpression<T> second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public T evaluate(Calculator<T> calc, int x) {
        return calculate(calc, first.evaluate(calc, x), second.evaluate(calc, x));
    }

    @Override
    public T evaluate(Calculator<T> calc, int x, int y, int z) {
        return calculate(calc, first.evaluate(calc, x, y, z), second.evaluate(calc, x, y, z));
    }

    protected T calculate(Calculator<T> calc, T x, T y) {
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
        sb.append("(");
        first.toString(sb);
        sb.append(" ").append(getOperator()).append(" ");
        second.toString(sb);
        sb.append(")");
    }

    @Override
    public String toMiniString() {
        StringBuilder sb = new StringBuilder();
        toMiniString(sb, -1, false, false);
        return sb.toString();
    }

    @Override
    public void toMiniString(StringBuilder sb, int prevRank, boolean isLeft, boolean prevAssociativity) {
        boolean bracketsCondition = (
                prevRank > getRank()
                        || (prevRank == getRank() && !isLeft && (!prevAssociativity || !getContinuity()))
        );
        sb.append(bracketsCondition ? "(" : "");
        first.toMiniString(sb, getRank(), true, getAssociativity());
        sb.append(" ").append(getOperator()).append(" ");
        second.toMiniString(sb, getRank(), false, getAssociativity());
        sb.append(bracketsCondition ? ")" : "");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        BinaryOperation<?> that = (BinaryOperation<?>) obj;
        return Objects.equals(first, that.first) && Objects.equals(second, that.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second, this.getClass());
    }

    protected abstract String getOperator();

    public abstract int getRank();

    protected abstract boolean getAssociativity();

    protected abstract boolean getContinuity();
}
