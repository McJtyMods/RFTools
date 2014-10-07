package com.mcjty.rftools.blocks.teleporter;

import com.mcjty.varia.Coordinate;

public class TransmitterInfo {
    private final Coordinate coordinate;
    private final String name;

    public TransmitterInfo(Coordinate coordinate, String name) {
        this.coordinate = coordinate;
        this.name = name;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public String getName() {
        return name;
    }
}
