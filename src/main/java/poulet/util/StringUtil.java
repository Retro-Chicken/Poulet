package poulet.util;


import java.util.*;
import java.util.stream.Collectors;

public class StringUtil {
    public static String indent(String s, int n) {
        String indent = "";

        for (int i = 0; i < n; i++)
            indent += " ";

        return indent + s.replaceAll("\n", "\n" + indent);
    }

    @SuppressWarnings("unchecked")
    public static <K extends Comparable<? super K>> String mapToStringWithNewlines(Map<K, ?> map) {
        List<String> items = new ArrayList<>();
        List<K> sortedKeys = new ArrayList<>(map.keySet());
        Collections.sort(sortedKeys);

        for (K key : sortedKeys) {
            Object value = map.get(key);
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

        String itemsString = String.join("\n", items);

        return "{\n" + indent(itemsString, 2) + "\n}";
    }
}
