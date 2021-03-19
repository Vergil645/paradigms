package expression;

import expression.calculator.Calculator;

public class Variable<T> implements CommonExpression<T> {
    private final Calculator<T> calc;
    private final String var;

    public Variable(Calculator<T> calc, String var) {
        this.calc = calc;
        this.var = var;
    }

    @Override
    public T evaluate(int x, int y, int z) {
        if (var.equals("x")) {
            return calc.valueOf(x);
        } else if (var.equals("y")) {
            return calc.valueOf(y);
        } else if (var.equals("z")) {
            return calc.valueOf(z);
        } else {
            return null;
        }
    }
}
