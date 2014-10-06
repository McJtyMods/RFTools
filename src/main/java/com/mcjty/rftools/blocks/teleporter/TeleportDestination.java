package com.mcjty.rftools.blocks.teleporter;

import com.mcjty.varia.Coordinate;

import java.util.HashSet;
import java.util.Set;

public class TeleportDestination {
    private final Coordinate coordinate;
    private final int dimension;
    private final Set<String> allowedPlayers;

    public TeleportDestination(Coordinate coordinate, int dimension, Set<String> allowedPlayers) {
        this.coordinate = coordinate;
        this.dimension = dimension;
        this.allowedPlayers = new HashSet<String>(allowedPlayers);
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public int getDimension() {
        return dimension;
    }

    public Set<String> getAllowedPlayers() {
        return allowedPlayers;
    }
}
