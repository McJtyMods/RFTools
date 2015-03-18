package com.mcjty.rftools;

import com.mcjty.varia.Coordinate;

/**
 * This class holds information on client-side only which are global to the mod.
 */
public class ClientInfo {
    private Coordinate selectedTE = null;
    private Coordinate destinationTE = null;

    private Coordinate hilightedBlock = null;
    private long expireHilight = 0;

    public void hilightBlock(Coordinate c, long expireHilight) {
        hilightedBlock = c;
        this.expireHilight = expireHilight;
    }

    public Coordinate getHilightedBlock() {
        return hilightedBlock;
    }

    public long getExpireHilight() {
        return expireHilight;
    }

    public Coordinate getSelectedTE() {
        return selectedTE;
    }

    public void setSelectedTE(Coordinate selectedTE) {
        this.selectedTE = selectedTE;
    }

    public Coordinate getDestinationTE() {
        return destinationTE;
    }

    public void setDestinationTE(Coordinate destinationTE) {
        this.destinationTE = destinationTE;
    }
}
