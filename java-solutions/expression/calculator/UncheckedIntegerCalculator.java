package expression.calculator;

public class UncheckedIntegerCalculator extends AbstractCalculator<Integer> {
    @Override
    public Integer valueOf(int arg) {
        return arg;
    }

    @Override
    protected Integer parse(String str) {
        return valueOf(Integer.parseInt(str));
    }

    @Override
    public Integer add(Integer arg1, Integer arg2) {
        return arg1 + arg2;
    }

    @Override
    public Integer subtract(Integer arg1, Integer arg2) {
        return arg1 - arg2;
    }

    @Override
    public Integer multiply(Integer arg1, Integer arg2) {
        return arg1 * arg2;
    }

    @Override
    public Integer divide(Integer arg1, Integer arg2) {
        return arg1 / arg2;
    }

    @Override
    public Integer negate(Integer arg) {
        return -arg;
    }

    @Override
    public Integer abs(Integer arg) {
        return arg >= 0 ? arg : -arg;
    }

    @Override
    public Integer square(Integer arg) {
        return arg * arg;
    }

    @Override
    public Integer mod(Integer arg1, Integer arg2) {
        return arg1 % arg2;
    }

    @Override
    public boolean isValidSymbol(char symbol) {
        return '0' <= symbol && symbol <= '9';
    }
}
