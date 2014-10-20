package com.mcjty.rftools.blocks.endergen;

import com.mcjty.entity.GenericEnergyHandlerTileEntity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

public class EndergenicTileEntity extends GenericEnergyHandlerTileEntity {

    public static final int CHARGE_IDLE = 0;
    public static final int CHARGE_HOLDING = -1;

    // The current chargingMode status.
    // CHARGE_IDLE means this entity is doing nothing.
    // A positive number means it is chargingMode up from 0 to 15. When it reaches 15 it will go back to idle unless
    // it was hit by an endergenic pearl in the mean time. In that case it goes to 'holding' state.
    // CHARGE_HOLDING means this entity is holding an endergenic pearl. Whie it does that it consumes
    // energy. If internal energy is depleted then the endergenic pearl is lost and the mode goes
    // back to idle.
    private int chargingMode = CHARGE_IDLE;

    public EndergenicTileEntity() {
        super(1000000, 20000, 20000);
    }

    @Override
    protected void checkStateServer() {
        super.checkStateServer();

        if (chargingMode == CHARGE_IDLE) {
            // Do nothing
            return;
        }

        if (chargingMode == CHARGE_HOLDING) {
            // Consume energy to keep the endergenic pearl.
            extractEnergy(ForgeDirection.DOWN, 1000, false);
            return;
        }

        // Else we're charging up.
        chargingMode++;
        if (chargingMode >= 16) {
            chargingMode = CHARGE_IDLE;
        }

//        receiveEnergy(ForgeDirection.DOWN, 1000, false);
    }

    public int getChargingMode() {
        return chargingMode;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);

        chargingMode = tagCompound.getInteger("charging");
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);

        tagCompound.setInteger("charging", chargingMode);
    }
}
