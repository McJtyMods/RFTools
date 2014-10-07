package com.mcjty.rftools.network;

import java.util.HashMap;
import java.util.Map;

public enum ArgumentType {
    TYPE_STRING(0),
    TYPE_INTEGER(1),
    TYPE_COORDINATE(2),
    TYPE_BOOLEAN(3);

    private final int index;
    private static final Map<Integer, ArgumentType> mapping = new HashMap<Integer, ArgumentType>();

    static {
        for (ArgumentType type : values()) {
            mapping.put(type.index, type);
        }
    }

    ArgumentType(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public static ArgumentType getType(int index) {
        return mapping.get(index);
    }
}
