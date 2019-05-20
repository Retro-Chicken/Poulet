package poulet;


import java.util.*;
import java.util.stream.Collectors;

public class Util {
    public static String indent(String s, int n) {
        String indent = "";

        for (int i = 0; i < n; i++)
            indent += " ";

        return indent + s.replaceAll("\n", "\n" + indent);
    }

    public static <K extends Comparable, V> String mapToStringWithNewlines(Map<K, V> map) {
        List<String> items = new ArrayList<>();
        List<K> sortedKeys = new ArrayList<>(map.keySet());
        Collections.sort(sortedKeys);

        for (K key : sortedKeys) {
            V value = map.get(key);
            String item = "" + key + "=";
            if (value instanceof Map) {
                Map innerMap = (Map) value;
                String innerString = mapToStringWithNewlines(innerMap);
                item += innerString;
            } else {
                item += value;
            }
            items.add(item);
        }

        String itemsString = items.stream().collect(Collectors.joining(",\n"));

        return "{\n" + indent(itemsString, 2) + "\n}";
    }
}
