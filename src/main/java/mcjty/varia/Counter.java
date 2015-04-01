package mcjty.varia;

import java.util.HashMap;

public class Counter<T> extends HashMap<T,Integer> {
    public void increment(T key) {
        if (containsKey(key)) {
            Integer a = get(key);
            put(key, a+1);
        } else {
            put(key, 1);
        }
    }
}
