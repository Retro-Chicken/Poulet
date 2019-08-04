package poulet.util;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ListUtil {
    public static <T, S> List<S> map(List<T> list, Function<T, S> function) {
        return list.stream().map(function).collect(Collectors.toList());
    }

    public static <T, S> List<S> flatMap(List<T> list, Function<T, ? extends List<S>> function) {
        return list.stream().flatMap(x -> function.apply(x).stream()).collect(Collectors.toList());
    }
}
