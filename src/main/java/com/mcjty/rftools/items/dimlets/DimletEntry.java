package com.mcjty.rftools.items.dimlets;

/**
* Created by jorrit on 20/11/14.
*/
public class DimletEntry {
    private final DimletKey key;
    private final int rfCreateCost;     // Overrides the type default. If -1 then use default.
    private final int rfMaintainCost;   // Overrides the type default. If -1 then use default.
    private final int tickCost;         // Overrides the type default. If -1 then use default.

    public DimletEntry(DimletKey key, int rfCreateCost, int rfMaintainCost, int tickCost) {
        this.key = key;
        this.rfCreateCost = rfCreateCost;
        this.rfMaintainCost = rfMaintainCost;
        this.tickCost = tickCost;
    }


    public DimletKey getKey() {
        return key;
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

        if (rfCreateCost != that.rfCreateCost) return false;
        if (rfMaintainCost != that.rfMaintainCost) return false;
        if (tickCost != that.tickCost) return false;
        if (!key.equals(that.key)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + rfCreateCost;
        result = 31 * result + rfMaintainCost;
        result = 31 * result + tickCost;
        return result;
    }
}
