package expression;

public interface CommonExpression<T> extends Expression<T>, TripleExpression<T> {
    String toString();

    void toString(StringBuilder sb);

    void toMiniString(StringBuilder sb, int prevRank, boolean isLeft, boolean prevAssociativity);

    boolean equals(Object obj);

    int hashCode();

    int getRank();
}
