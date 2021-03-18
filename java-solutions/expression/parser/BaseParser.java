package expression.parser;

public abstract class BaseParser {
    private static final char END = '\0';
    private final CharSource source;
    private char ch;
    private int sourcePosition = 0;
    private int sbPos = 0;
    private final StringBuilder sb = new StringBuilder();

    protected BaseParser(final CharSource source) {
        this.source = source;
        ch = source.hasNext() ? source.next() : END;
    }

    protected char getChar() {
        return sbPos < sb.length() ? sb.charAt(sbPos) : ch;
    }

    protected int getPos() {
        return sourcePosition;
    }

    protected boolean eof() {
        return getChar() == END;
    }

    protected boolean between(final char from, final char to) {
        return from <= getChar() && getChar() <= to;
    }

    protected char nextChar() {
        if (sbPos < sb.length()) {
            sourcePosition++;
            return sb.charAt(sbPos++);
        } else {
            if (sb.length() > 0) {
                sbPos = 0;
                sb.setLength(0);
            }
            char tmp = ch;
            sourcePosition += eof() ? 0 : 1;
            ch = source.hasNext() ? source.next() : END;
            return tmp;
        }
    }

    protected boolean test(final char expected) {
        if (getChar() == expected) {
            nextChar();
            return true;
        }
        return false;
    }

    protected String substring(final int len) {
        readSb(sbPos + len);
        return sbPos + len > sb.length() ? String.valueOf(END) : sb.substring(sbPos, sbPos + len);
    }

    protected boolean test(final String expected, boolean condition) {
        if (expected.equals(substring(expected.length())) && condition) {
            sourcePosition += expected.length();
            sbPos = sbPos + expected.length();
            return true;
        }
        return false;
    }

    protected char charAt(final int offset) {
        readSb(sbPos + offset + 1);
        return sbPos + offset >= sb.length() ? END : sb.charAt(sbPos + offset);
    }

    private void readSb(final int newLength) {
        while (!eof() && newLength > sb.length()) {
            sb.append(ch);
            ch = source.hasNext() ? source.next() : END;
        }
    }
}
