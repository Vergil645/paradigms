package expression.calculator;

public class ByteCalculator extends AbstractCalculator<Byte> {
    @Override
    public Byte valueOf(int arg) {
        return (byte) arg;
    }

    @Override
    protected Byte parse(String str) {
        return Byte.parseByte(str);
    }

    @Override
    public Byte add(Byte arg1, Byte arg2) {
        return (byte) (arg1 + arg2);
    }

    @Override
    public Byte subtract(Byte arg1, Byte arg2) {
        return (byte) (arg1 - arg2);
    }

    @Override
    public Byte multiply(Byte arg1, Byte arg2) {
        return (byte) (arg1 * arg2);
    }

    @Override
    public Byte divide(Byte arg1, Byte arg2) {
        return (byte) (arg1 / arg2);
    }

    @Override
    public Byte negate(Byte arg) {
        return (byte) (-arg);
    }

    @Override
    public Byte abs(Byte arg) {
        return arg >= 0 ? arg : negate(arg);
    }

    @Override
    public Byte square(Byte arg) {
        return (byte) (arg * arg);
    }

    @Override
    public Byte mod(Byte arg1, Byte arg2) {
        return (byte) (arg1 % arg2);
    }

    @Override
    public boolean isValidSymbol(char symbol) {
        return '0' <= symbol && symbol <= '9';
    }
}
