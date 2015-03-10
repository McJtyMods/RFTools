package com.mcjty.rftools.items.dimlets;

/**
* Created by jorrit on 8/12/14.
*/
public class DimletKey {
    private final DimletType type;
    private final String name;

    public DimletKey(DimletType type, String name) {
        this.type = type;
        this.name = name;
    }

    public DimletType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DimletKey dimletKey = (DimletKey) o;

        if (type != dimletKey.type) {
            return false;
        }
        if (!name.equals(dimletKey.name)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return type.getOpcode() + name;
    }

    public static DimletKey parseKey(String skey) {
        String opcode = skey.substring(0, 1);
        String name = skey.substring(1);
        return new DimletKey(DimletType.getTypeByOpcode(opcode), name);
    }
}
