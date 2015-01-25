package com.mcjty.rftools.blocks.screens;

import com.mcjty.entity.GenericEnergyHandlerTileEntity;
import com.mcjty.rftools.network.Argument;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Map;

public class ScreenControllerTileEntity extends GenericEnergyHandlerTileEntity {

    public ScreenControllerTileEntity() {
        super(ScreenConfiguration.CONTROLLER_MAXENERGY, ScreenConfiguration.CONTROLLER_RECEIVEPERTICK);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
    }

    @Override
    public boolean execute(String command, Map<String, Argument> args) {
        boolean rc = super.execute(command, args);
        if (rc) {
            return true;
        }
//        if (CMD_SETTINGS.equals(command)) {
//            setRfOn(args.get("on").getInteger());
//            setRfOff(args.get("off").getInteger());
//            return true;
//        }
        return false;
    }
}
