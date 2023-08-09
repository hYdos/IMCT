package gg.generations.imct.util;

import java.util.HashMap;
import java.util.function.Consumer;

public class Utils {
    public static <T> T create(T t, Consumer<T> consumer) {
        consumer.accept(t);
        return t;
    }
}
