package com.mcjty.rftools.items.dimlets;

/**
* Created by jorrit on 20/11/14.
*/
public class DimletEntry {
    private final DimletType type;
    private final String name;

    public DimletEntry(DimletType type, String name) {
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

        DimletEntry that = (DimletEntry) o;

        if (!name.equals(that.name)) return false;
        if (type != that.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }
}
