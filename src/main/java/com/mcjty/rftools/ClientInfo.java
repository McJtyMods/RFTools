package com.mcjty.rftools;

import com.mcjty.rftools.blocks.endergen.EndergenicTileEntity;
import com.mcjty.varia.Coordinate;

/**
 * This class holds information on client-side only which are global to the mod.
 */
public class ClientInfo {
    private EndergenicTileEntity selectedEndergenicTileEntity = null;

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

    public EndergenicTileEntity getSelectedEndergenicTileEntity() {
        return selectedEndergenicTileEntity;
    }

    public void setSelectedEndergenicTileEntity(EndergenicTileEntity selectedEndergenicTileEntity) {
        this.selectedEndergenicTileEntity = selectedEndergenicTileEntity;
    }
}
