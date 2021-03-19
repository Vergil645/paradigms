package expression.parser;

import expression.CommonExpression;
import expression.Variable;

import java.util.AbstractMap;
import java.util.Map;

public class VariablesList<T> {
    public final int maxVarLength;

    public VariablesList() {
        int max = 0;
        for (String str : NAMES.keySet()) {
            max = Math.max(max, str.length());
        }
        maxVarLength = max;
    }

    public final Map<String, CommonExpression<T>> NAMES = Map.ofEntries(
            new AbstractMap.SimpleEntry<>("x", new Variable<>("x")),
            new AbstractMap.SimpleEntry<>("y", new Variable<>("y")),
            new AbstractMap.SimpleEntry<>("z", new Variable<>("z"))
    );
}
