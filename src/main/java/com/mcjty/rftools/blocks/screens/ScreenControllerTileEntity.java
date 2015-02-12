package com.mcjty.rftools.blocks.screens;

import com.mcjty.entity.GenericEnergyHandlerTileEntity;
import com.mcjty.rftools.network.Argument;
import com.mcjty.varia.Coordinate;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ScreenControllerTileEntity extends GenericEnergyHandlerTileEntity {

    public static final String CMD_SCAN = "scan";
    public static final String CMD_DETACH = "detach";

    private List<Coordinate> connectedScreens = new ArrayList<Coordinate>();
    private int tickCounter = 20;

    public ScreenControllerTileEntity() {
        super(ScreenConfiguration.CONTROLLER_MAXENERGY, ScreenConfiguration.CONTROLLER_RECEIVEPERTICK);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        int[] xes = tagCompound.getIntArray("screensx");
        int[] yes = tagCompound.getIntArray("screensy");
        int[] zes = tagCompound.getIntArray("screensz");
        connectedScreens.clear();
        for (int i = 0 ; i < xes.length ; i++) {
            connectedScreens.add(new Coordinate(xes[i], yes[i], zes[i]));
        }
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        int[] xes = new int[connectedScreens.size()];
        int[] yes = new int[connectedScreens.size()];
        int[] zes = new int[connectedScreens.size()];
        for (int i = 0 ; i < connectedScreens.size() ; i++) {
            Coordinate c = connectedScreens.get(i);
            xes[i] = c.getX();
            yes[i] = c.getY();
            zes[i] = c.getZ();
        }
        tagCompound.setIntArray("screensx", xes);
        tagCompound.setIntArray("screensy", yes);
        tagCompound.setIntArray("screensz", zes);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
    }

    @Override
    protected void checkStateServer() {
        tickCounter--;
        if (tickCounter > 0) {
            return;
        }
        tickCounter = 20;
        int rf = getEnergyStored(ForgeDirection.DOWN);
        int rememberRf = rf;
        boolean fixesAreNeeded = false;
        for (Coordinate c : connectedScreens) {
            TileEntity te = worldObj.getTileEntity(c.getX(), c.getY(), c.getZ());
            if (te instanceof ScreenTileEntity) {
                ScreenTileEntity screenTileEntity = (ScreenTileEntity) te;
                int rfModule = screenTileEntity.getTotalRfPerTick() * 20;

                if (rfModule > rf) {
                    screenTileEntity.setPower(false);
                } else {
                    rf -= rfModule;
                    screenTileEntity.setPower(true);
                }
            } else {
                // This coordinate is no longer a valid screen. We need to update.
                fixesAreNeeded = true;
            }
        }
        if (rf < rememberRf) {
            extractEnergy(ForgeDirection.DOWN, rememberRf - rf, false);
        }

        if (fixesAreNeeded) {
            List<Coordinate> newScreens = new ArrayList<Coordinate>();
            for (Coordinate c : connectedScreens) {
                TileEntity te = worldObj.getTileEntity(c.getX(), c.getY(), c.getZ());
                if (te instanceof ScreenTileEntity) {
                    newScreens.add(c);
                }
            }
            connectedScreens = newScreens;
            markDirty();
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
    }

    private void scan() {
        detach();
        int radius = 32 + (int) (getInfusedFactor() * 32);

        for (int y = yCoord - 16 ; y <= yCoord + 16 ; y++) {
            if (y >= 0 && y < 256) {
                for (int x = xCoord - radius; x <= xCoord + radius; x++) {
                    for (int z = zCoord - radius; z <= zCoord + radius; z++) {
                        TileEntity te = worldObj.getTileEntity(x, y, z);
                        if (te instanceof ScreenTileEntity) {
                            if (!((ScreenTileEntity) te).isConnected()) {
                                connectedScreens.add(new Coordinate(x, y, z));
                                ((ScreenTileEntity) te).setConnected(true);
                            }
                        }
                    }
                }
            }
        }
        markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    public void detach() {
        for (Coordinate c : connectedScreens) {
            TileEntity te = worldObj.getTileEntity(c.getX(), c.getY(), c.getZ());
            if (te instanceof ScreenTileEntity) {
                ((ScreenTileEntity) te).setPower(false);
                ((ScreenTileEntity) te).setConnected(false);
            }
        }

        connectedScreens.clear();
        markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    public List<Coordinate> getConnectedScreens() {
        return connectedScreens;
    }

    @Override
    public boolean execute(String command, Map<String, Argument> args) {
        boolean rc = super.execute(command, args);
        if (rc) {
            return true;
        }
        if (CMD_SCAN.equals(command)) {
            scan();
            return true;
        } else if (CMD_DETACH.equals(command)) {
            detach();
            return true;
        }
        return false;
    }
}
