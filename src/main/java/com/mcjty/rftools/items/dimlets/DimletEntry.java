package com.mcjty.rftools.items.dimlets;

public class DimletEntry {
    private final DimletKey key;
    private final int rfCreateCost;     // Overrides the type default
    private final int rfMaintainCost;   // Overrides the type default
    private final int tickCost;         // Overrides the type default
    private final int rarity;           // Overrides the type default
    private final boolean expensive;

    public DimletEntry(DimletKey key, int rfCreateCost, int rfMaintainCost, int tickCost, int rarity, boolean expensive) {
        this.key = key;
        this.rfCreateCost = rfCreateCost;
        this.rfMaintainCost = rfMaintainCost;
        this.tickCost = tickCost;
        this.rarity = rarity;
        this.expensive = expensive;
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

    public int getRarity() {
        return rarity;
    }

    public boolean isExpensive() {
        return expensive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DimletEntry that = (DimletEntry) o;

        if (expensive != that.expensive) return false;
        if (rarity != that.rarity) return false;
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
        result = 31 * result + rarity;
        result = 31 * result + (expensive ? 1 : 0);
        return result;
    }
}
