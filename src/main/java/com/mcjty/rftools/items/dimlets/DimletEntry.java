package com.mcjty.rftools.items.dimlets;

/**
* Created by jorrit on 20/11/14.
*/
public class DimletEntry {
    private final DimletType type;
    private final String name;
    private final int rfCreateCost;     // Overrides the type default. If -1 then use default.
    private final int rfMaintainCost;   // Overrides the type default. If -1 then use default.
    private final int tickCost;         // Overrides the type default. If -1 then use default.

    public DimletEntry(DimletType type, String name, int rfCreateCost, int rfMaintainCost, int tickCost) {
        this.type = type;
        this.name = name;
        this.rfCreateCost = rfCreateCost;
        this.rfMaintainCost = rfMaintainCost;
        this.tickCost = tickCost;
    }

    public DimletType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public int getRfCreateCost() {
        return rfCreateCost;
    }

    public int getRfMaintainCost() {
        return rfMaintainCost;
    }

    public int getTickCost() {
        return tickCost;
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
