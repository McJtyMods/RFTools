package mcjty.varia;

import java.util.HashMap;

public class Counter<T> extends HashMap<T,Integer> {
    public void increment(T key) {
        increment(key, 1);
    }

    public void increment(T key, int amount) {
        if (containsKey(key)) {
            Integer a = get(key);
            put(key, a+amount);
        } else {
            put(key, amount);
        }
    }
}
