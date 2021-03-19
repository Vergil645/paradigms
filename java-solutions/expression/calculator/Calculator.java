package expression.calculator;

public interface Calculator<T> {
    T valueOf(int arg);

    T valueOf(String str) throws IllegalArgumentException;

    T add(T arg1, T arg2);

    T subtract(T arg1, T arg2);

    T multiply(T arg1, T arg2);

    T divide(T arg1, T arg2);

    T negate(T arg);

    T abs(T arg);

    T square(T arg);

    T mod (T arg1, T arg2);

    boolean isValidSymbol(char symbol);
}
