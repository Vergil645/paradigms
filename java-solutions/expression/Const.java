package expression;

import expression.calculator.Calculator;

import java.util.Objects;

public class Const<T> implements CommonExpression<T> {
    private final T value;

    public Const(T value) {
        this.value = value;
    }

    @Override
    public T evaluate(Calculator<T> calc, int x) {
        return value;
    }

    @Override
    public T evaluate(Calculator<T> calc, int x, int y, int z) {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public void toString(StringBuilder sb) {
        sb.append(value.toString());
    }

    @Override
    public String toMiniString() {
        return value.toString();
    }

    @Override
    public void toMiniString(StringBuilder sb, int prevRank, boolean isLeft, boolean prevAssociativity) {
        sb.append(value.toString());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Const<?> aConst = (Const<?>) obj;
        return Objects.equals(value, aConst.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public int getRank() {
        return 0;
    }
}
