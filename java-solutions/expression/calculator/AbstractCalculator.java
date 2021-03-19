package expression.calculator;

public abstract class AbstractCalculator<T> implements Calculator<T> {
    @Override
    public T valueOf(int arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T valueOf(String str) throws IllegalArgumentException {
        try {
            return parse(str);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid argument: cannot cast " + str + " to required type");
        }
    }

    protected T parse(String str) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T add(T arg1, T arg2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T subtract(T arg1, T arg2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T multiply(T arg1, T arg2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T divide(T arg1, T arg2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T negate(T arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T abs(T arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T square(T arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T mod (T arg1, T arg2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isValidSymbol(char symbol) {
        throw new UnsupportedOperationException();
    }
}
