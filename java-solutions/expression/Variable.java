package expression;

import expression.calculator.Calculator;

public class Variable<T> implements TripleExpression<T> {
    private final String var;

    public Variable(String var) {
        this.var = var;
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
}
