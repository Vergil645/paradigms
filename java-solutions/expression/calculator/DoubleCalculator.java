package expression.calculator;

public class DoubleCalculator extends AbstractCalculator<Double> {
    @Override
    public Double valueOf(int arg) {
        return (double) arg;
    }

    @Override
    protected Double parse(String str) {
        return Double.parseDouble(str);
    }

    @Override
    public Double add(Double arg1, Double arg2) {
        return arg1 + arg2;
    }

    @Override
    public Double subtract(Double arg1, Double arg2) {
        return arg1 - arg2;
    }

    @Override
    public Double multiply(Double arg1, Double arg2) {
        return arg1 * arg2;
    }

    @Override
    public Double divide(Double arg1, Double arg2) {
        return arg1 / arg2;
    }

    @Override
    public Double negate(Double arg) {
        return -arg;
    }

    @Override
    public Double abs(Double arg) {
        return arg >= 0 ? arg : -arg;
    }

    @Override
    public Double square(Double arg) {
        return arg * arg;
    }

    @Override
    public Double mod (Double arg1, Double arg2) {
        return arg1 % arg2;
    }

    @Override
    public boolean isValidSymbol(char symbol) {
        return symbol == '.' || ('0' <= symbol && symbol <= '9');
    }
}
