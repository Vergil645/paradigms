package expression.calculator;

import expression.exceptions.DivisionByZeroException;
import java.math.BigInteger;

public class BigIntegerCalculator extends AbstractCalculator<BigInteger> {
    @Override
    public BigInteger valueOf(int arg) {
        return BigInteger.valueOf(arg);
    }

    @Override
    protected BigInteger parse(String str) {
        return new BigInteger(str);
    }

    @Override
    public BigInteger add(BigInteger arg1, BigInteger arg2) {
        return arg1.add(arg2);
    }

    @Override
    public BigInteger subtract(BigInteger arg1, BigInteger arg2) {
        return arg1.subtract(arg2);
    }

    @Override
    public BigInteger multiply(BigInteger arg1, BigInteger arg2) {
        return arg1.multiply(arg2);
    }

    @Override
    public BigInteger divide(BigInteger arg1, BigInteger arg2) {
        if (arg2.equals(BigInteger.ZERO)) {
            throw new DivisionByZeroException(String.format("Division by zero: %d / %d", arg1, arg2));
        }
        return arg1.divide(arg2);
    }

    @Override
    public BigInteger negate(BigInteger arg) {
        return arg.negate();
    }

    @Override
    public BigInteger abs(BigInteger arg) {
        return arg.abs();
    }

    @Override
    public BigInteger square(BigInteger arg) {
        return arg.multiply(arg);
    }

    @Override
    public BigInteger mod (BigInteger arg1, BigInteger arg2) {
        return arg1.mod(arg2);
    }

    @Override
    public boolean isValidSymbol(char symbol) {
        return '0' <= symbol && symbol <= '9';
    }
}
