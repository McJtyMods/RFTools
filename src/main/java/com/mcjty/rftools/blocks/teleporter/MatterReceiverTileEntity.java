package com.mcjty.rftools.blocks.teleporter;

import com.mcjty.entity.GenericEnergyHandlerTileEntity;
import com.mcjty.rftools.network.Argument;
import com.mcjty.varia.Coordinate;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Map;

public class MatterReceiverTileEntity extends GenericEnergyHandlerTileEntity {

    public static final int MAXENERGY = 100000;
    public static final int RECEIVEPERTICK = 500;

    public static final String CMD_SETNAME = "setName";

    private String name = null;

    public MatterReceiverTileEntity() {
        super(MAXENERGY, RECEIVEPERTICK);
    }

    public String getName() {
        return name == null ? "" : name;
    }

    public void setName(String name) {
        this.name = name;
        TeleportDestinations destinations = TeleportDestinations.getDestinations(worldObj);
        TeleportDestination destination = destinations.getDestination(new Coordinate(xCoord, yCoord, zCoord), 0);
        if (destination != null) {
            destination.setName(name);
            destinations.save(worldObj);
        }

        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    public int checkStatus() {
        Block block = worldObj.getBlock(xCoord, yCoord-1, zCoord);
        if (!block.isAir(worldObj, xCoord, yCoord-1, zCoord)) {
            return DialingDeviceTileEntity.DIAL_RECEIVER_BLOCKED_MASK;
        }
        block = worldObj.getBlock(xCoord, yCoord-2, zCoord);
        if (!block.isAir(worldObj, xCoord, yCoord-2, zCoord)) {
            return DialingDeviceTileEntity.DIAL_RECEIVER_BLOCKED_MASK;
        }
        return DialingDeviceTileEntity.DIAL_OK;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        name = tagCompound.getString("tpName");
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        if (name != null && !name.isEmpty()) {
            tagCompound.setString("tpName", name);
        }
    }

    @Override
    public boolean execute(String command, Map<String, Argument> args) {
        boolean rc = super.execute(command, args);
        if (rc) {
            return true;
        }
        if (CMD_SETNAME.equals(command)) {
            setName(args.get("name").getString());
            return true;
        }
        return false;
    }
}
