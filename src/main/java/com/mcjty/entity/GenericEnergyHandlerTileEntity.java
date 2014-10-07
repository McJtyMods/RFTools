package com.mcjty.entity;

import cofh.api.energy.EnergyStorage;
import cofh.api.energy.IEnergyHandler;
import com.mcjty.rftools.network.Argument;
import com.mcjty.rftools.network.CommandHandler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Map;

public class GenericEnergyHandlerTileEntity extends GenericTileEntity implements IEnergyHandler, CommandHandler {

    private EnergyStorage storage;

    private int oldRF = -1;             // Optimization for client syncing
    private int currentRF = 0;


    public GenericEnergyHandlerTileEntity(int maxEnergy, int maxReceive) {
        storage = new EnergyStorage(maxEnergy);
        storage.setMaxReceive(maxReceive);
    }

    @Override
    public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate) {
        return storage.receiveEnergy(maxReceive, simulate);
    }

    @Override
    public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate) {
        return storage.extractEnergy(maxExtract, simulate);
    }

    @Override
    public int getEnergyStored(ForgeDirection from) {
        return storage.getEnergyStored();
    }

    @Override
    public int getMaxEnergyStored(ForgeDirection from) {
        return storage.getMaxEnergyStored();
    }

    @Override
    public boolean canConnectEnergy(ForgeDirection from) {
        return true;
    }


    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        storage.readFromNBT(tagCompound);
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        storage.writeToNBT(tagCompound);
    }


    public int getOldRF() {
        return oldRF;
    }

    public void setOldRF(int oldRF) {
        this.oldRF = oldRF;
    }

    public int getCurrentRF() {
        return currentRF;
    }

    public void setCurrentRF(int currentRF) {
        this.currentRF = currentRF;
    }

    @Override
    public boolean execute(String command, Map<String, Argument> args) {
        return false;
    }
}
