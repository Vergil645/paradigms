package expression;

import expression.calculator.Calculator;

import java.util.Objects;

public class Variable<T> implements CommonExpression<T> {
    private final String var;

    public Variable(String var) {
        this.var = var;
    }

    @Override
    public T evaluate(Calculator<T> calc, int x) {
        return calc.valueOf(x);
    }

    @Override
    public T evaluate(Calculator<T> calc, int x, int y, int z) {
        if (var.equals("x")) {
            return calc.valueOf(x);
        } else if (var.equals("y")) {
            return calc.valueOf(y);
        } else {
            return calc.valueOf(z);
        }
    }

    @Override
    public String toString() {
        return var;
    }

    @Override
    public void toString(StringBuilder sb) {
        sb.append(var);
    }

    @Override
    public String toMiniString() {
        return var;
    }

    @Override
    public void toMiniString(StringBuilder sb, int prevRank, boolean isLeft, boolean prevAssociativity) {
        sb.append(var);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Variable<?> variable = (Variable<?>) obj;
        return Objects.equals(var, variable.var);
    }

    @Override
    public int hashCode() {
        return Objects.hash(var);
    }

    @Override
    public int getRank() {
        return 0;
    }
}
