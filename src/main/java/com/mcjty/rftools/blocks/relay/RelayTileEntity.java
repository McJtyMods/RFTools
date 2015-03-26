package com.mcjty.rftools.blocks.relay;

import cofh.api.energy.IEnergyConnection;
import com.mcjty.entity.GenericEnergyHandlerTileEntity;
import com.mcjty.rftools.blocks.BlockTools;
import com.mcjty.rftools.network.Argument;
import com.mcjty.varia.EnergyTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Map;

public class RelayTileEntity extends GenericEnergyHandlerTileEntity {

    public static final int MAXENERGY = 30000;
    public static final int RECEIVEPERTICK = 20000;

    public static final String CMD_SETTINGS = "settings";

    private int rfOn = 1000;
    private int rfOff = 0;

    public RelayTileEntity() {
        super(MAXENERGY, RECEIVEPERTICK);
    }

    @Override
    protected void checkStateServer() {
        super.checkStateServer();

        int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
        int rf;
        if (BlockTools.getRedstoneSignal(meta)) {
            rf = rfOn;
        } else {
            rf = rfOff;
        }

        if (rf <= 0) {
            return;
        }

        int energyStored = getEnergyStored(ForgeDirection.DOWN);
        if (energyStored <= 0) {
            return;
        }

        for (int i = 0 ; i < 6 ; i++) {
            ForgeDirection dir = ForgeDirection.getOrientation(i);
            TileEntity te = worldObj.getTileEntity(xCoord+dir.offsetX, yCoord+dir.offsetY, zCoord+dir.offsetZ);
            if (EnergyTools.isEnergyTE(te)) {
                IEnergyConnection connection = (IEnergyConnection) te;
                ForgeDirection opposite = dir.getOpposite();
                if (connection.canConnectEnergy(opposite)) {
                    int rfToGive;
                    if (rf <= energyStored) {
                        rfToGive = rf;
                    } else {
                        rfToGive = energyStored;
                    }

                    int received = EnergyTools.receiveEnergy(te, opposite, rfToGive);
                    energyStored -= extractEnergy(ForgeDirection.DOWN, received, false);
                    if (energyStored <= 0) {
                        return;
                    }
                }
            }
        }
    }

    public int getRfOn() {
        return rfOn;
    }

    public void setRfOn(int rfOn) {
        this.rfOn = rfOn;
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    public int getRfOff() {
        return rfOff;
    }

    public void setRfOff(int rfOff) {
        this.rfOff = rfOff;
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        rfOn = tagCompound.getInteger("rfOn");
        rfOff = tagCompound.getInteger("rfOff");
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.setInteger("rfOn", rfOn);
        tagCompound.setInteger("rfOff", rfOff);
    }

    @Override
    public boolean execute(String command, Map<String, Argument> args) {
        boolean rc = super.execute(command, args);
        if (rc) {
            return true;
        }
        if (CMD_SETTINGS.equals(command)) {
            setRfOn(args.get("on").getInteger());
            setRfOff(args.get("off").getInteger());
            return true;
        }
        return false;
    }
}
