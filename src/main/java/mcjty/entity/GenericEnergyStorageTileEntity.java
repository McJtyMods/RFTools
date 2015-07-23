package mcjty.entity;

import cofh.api.energy.EnergyStorage;
import mcjty.rftools.network.Argument;
import mcjty.network.PacketHandler;
import mcjty.rftools.network.PacketRequestIntegerFromServer;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Map;

public class GenericEnergyStorageTileEntity extends GenericTileEntity {

    public static final String CMD_GETENERGY = "getEnergy";
    public static final String CLIENTCMD_GETENERGY = "getEnergy";

    protected EnergyStorage storage;

    private static int currentRF = 0;

    private int requestRfDelay = 3;

    public void modifyEnergyStored(int energy) {
        storage.modifyEnergyStored(energy);
    }

    public GenericEnergyStorageTileEntity(int maxEnergy, int maxReceive) {
        storage = new EnergyStorage(maxEnergy);
        storage.setMaxReceive(maxReceive);
    }

    public GenericEnergyStorageTileEntity(int maxEnergy, int maxReceive, int maxExtract) {
        storage = new EnergyStorage(maxEnergy);
        storage.setMaxReceive(maxReceive);
        storage.setMaxExtract(maxExtract);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        storage.readFromNBT(tagCompound);
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        storage.writeToNBT(tagCompound);
    }

    public static int getCurrentRF() {
        return currentRF;
    }

    public static void setCurrentRF(int currentRF) {
        GenericEnergyStorageTileEntity.currentRF = currentRF;
    }

    // Request the RF from the server. This has to be called on the client side.
    public void requestRfFromServer() {
        requestRfDelay--;
        if (requestRfDelay > 0) {
            return;
        }
        requestRfDelay = 3;
        PacketHandler.INSTANCE.sendToServer(new PacketRequestIntegerFromServer(xCoord, yCoord, zCoord,
                CMD_GETENERGY,
                CLIENTCMD_GETENERGY));
    }

    @Override
    public Integer executeWithResultInteger(String command, Map<String, Argument> args) {
        Integer rc = super.executeWithResultInteger(command, args);
        if (rc != null) {
            return rc;
        }
        if (CMD_GETENERGY.equals(command)) {
            return storage.getEnergyStored();
        }
        return null;
    }

    @Override
    public boolean execute(String command, Integer result) {
        boolean rc = super.execute(command, result);
        if (rc) {
            return true;
        }
        if (CLIENTCMD_GETENERGY.equals(command)) {
            setCurrentRF(result);
            return true;
        }
        return false;
    }
}
