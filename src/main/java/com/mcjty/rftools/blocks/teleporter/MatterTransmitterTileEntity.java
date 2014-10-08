package com.mcjty.rftools.blocks.teleporter;

import com.mcjty.entity.GenericEnergyHandlerTileEntity;
import com.mcjty.rftools.blocks.ModBlocks;
import com.mcjty.rftools.network.Argument;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Map;

public class MatterTransmitterTileEntity extends GenericEnergyHandlerTileEntity {

    public static final int MAXENERGY = 1000000;
    public static final int RECEIVEPERTICK = 1000;

    public static final String CMD_SETNAME = "setName";
    public static final String CMD_DIAL = "dial";

    private String name = null;

    public MatterTransmitterTileEntity() {
        super(MAXENERGY, RECEIVEPERTICK);
    }

    public String getName() {
        return name == null ? "" : name;
    }

    public void setName(String name) {
        this.name = name;
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
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

    private void dial() {
        worldObj.setBlock(xCoord, yCoord+1, zCoord, ModBlocks.teleportBeamBlock, 0, 2);
        worldObj.setBlock(xCoord, yCoord+2, zCoord, ModBlocks.teleportBeamBlock, 0, 2);
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
        } else if (CMD_DIAL.equals(command)) {
            dial();
            return true;
        }
        return false;
    }

}
