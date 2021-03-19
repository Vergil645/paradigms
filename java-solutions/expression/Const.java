package expression;

public class Const<T> implements CommonExpression<T> {
    private final T value;

    public Const(T value) {
        this.value = value;
    }

    @Override
    public T evaluate(int x, int y, int z) {
        return value;
    }
}
