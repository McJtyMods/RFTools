package com.mcjty.rftools.blocks.teleporter;

import com.mcjty.varia.Coordinate;

public class TeleportDestinationKey {
    private final Coordinate coordinate;
    private final int dimension;

    public TeleportDestinationKey(Coordinate coordinate, int dimension) {
        this.coordinate = coordinate;
        this.dimension = dimension;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public int getDimension() {
        return dimension;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TeleportDestinationKey that = (TeleportDestinationKey) o;

        if (dimension != that.dimension) return false;
        if (!coordinate.equals(that.coordinate)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = coordinate.hashCode();
        result = 31 * result + dimension;
        return result;
    }
}
