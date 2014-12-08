package com.mcjty.rftools.items.dimlets;

/**
* Created by jorrit on 8/12/14.
*/
public class DimletKey {
    final DimletType type;
    final String name;

    DimletKey(DimletType type, String name) {
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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DimletKey dimletKey = (DimletKey) o;

        if (!name.equals(dimletKey.name)) return false;
        if (type != dimletKey.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }
}
