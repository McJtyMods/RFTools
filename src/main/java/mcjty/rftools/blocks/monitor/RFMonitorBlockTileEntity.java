package mcjty.rftools.blocks.monitor;

import mcjty.lib.entity.GenericTileEntity;
import mcjty.lib.network.Argument;
import mcjty.rftools.varia.EnergyTools;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ITickable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RFMonitorBlockTileEntity extends GenericTileEntity implements ITickable {
    // Data that is saved
    private BlockPos monitor;
    private RFMonitorMode alarmMode = RFMonitorMode.MODE_OFF;
    private int alarmLevel = 0;             // The level (in percentage) at which we give an alarm

    public static final String CMD_GETADJACENTBLOCKS = "getAdj";
    public static final String CLIENTCMD_ADJACENTBLOCKSREADY = "adjReady";

    // Temporary data
    private int counter = 20;

    private int rflevel = 0;
    private boolean inAlarm = false;

    public RFMonitorMode getAlarmMode() {
        return alarmMode;
    }

    public int getAlarmLevel() {
        return alarmLevel;
    }

    public void setAlarm(RFMonitorMode mode, int level) {
        alarmMode = mode;
        alarmLevel = level;
        markDirtyClient();
    }

    public BlockPos getMonitor() {
        return monitor;
    }

    public boolean isValid() {
        return monitor != null;
    }

    public void setInvalid() {
        if (monitor == null) {
            return;
        }
        monitor = null;
        if (rflevel != 0) {
            rflevel = 0;
            markDirtyClient();
        } else {
            markDirty();
        }
        setRedstoneOut(false);
    }

    public void setMonitor(BlockPos c) {
        monitor = c;
        markDirtyClient();
    }

    public int getRflevel() {
        return rflevel;
    }

    public List<BlockPos> findAdjacentBlocks() {
        int x = getPos().getX();
        int y = getPos().getY();
        int z = getPos().getZ();
        List<BlockPos> adjacentBlocks = new ArrayList<>();
        for (int dy = -1 ; dy <= 1 ; dy++) {
            int yy = y + dy;
            if (yy >= 0 && yy < worldObj.getHeight()) {
                for (int dz = -1 ; dz <= 1 ; dz++) {
                    int zz = z + dz;
                    for (int dx = -1 ; dx <= 1 ; dx++) {
                        int xx = x + dx;
                        if (dx != 0 || dy != 0 || dz != 0) {
                            TileEntity tileEntity = worldObj.getTileEntity(new BlockPos(xx, yy, zz));
                            if (tileEntity != null) {
                                if (EnergyTools.isEnergyTE(tileEntity)) {
                                    adjacentBlocks.add(new BlockPos(xx, yy, zz));
                                }
                            }
                        }
                    }
                }
            }
        }
        return adjacentBlocks;
    }

    @Override
    public void update() {
        if (!worldObj.isRemote) {
            checkStateServer();
        }
    }

    private void checkStateServer() {
        if (!isValid()) {
            counter = 1;
            return;
        }

        counter--;
        if (counter > 0) {
            return;
        }
        counter = 20;

        TileEntity tileEntity = worldObj.getTileEntity(monitor);
        if (!EnergyTools.isEnergyTE(tileEntity)) {
            setInvalid();
            return;
        }
        EnergyTools.EnergyLevelMulti energy = EnergyTools.getEnergyLevelMulti(tileEntity);
        long maxEnergy = energy.getMaxEnergy();
        int ratio = 0;  // Will be set as metadata;
        boolean alarm = false;

        if (maxEnergy > 0) {
            long stored = energy.getEnergy();
            ratio = (int) (1 + (stored * 5) / maxEnergy);
            if (ratio < 1) {
                ratio = 1;
            } else if (ratio > 5) {
                ratio = 5;
            }

            switch (alarmMode) {
                case MODE_OFF:
                    alarm = false;
                    break;
                case MODE_LESS:
                    alarm = ((stored * 100 / maxEnergy) < alarmLevel);
                    break;
                case MODE_MORE:
                    alarm = ((stored * 100 / maxEnergy) > alarmLevel);
                    break;
            }

        }

        if (rflevel != ratio) {
            rflevel = ratio;
            markDirtyClient();
        }
        if (alarm != inAlarm) {
            inAlarm = alarm;
            setRedstoneOut(inAlarm);
            markDirty();
        }
    }

    private void setRedstoneOut(boolean a) {
        IBlockState state = worldObj.getBlockState(getPos());
        worldObj.setBlockState(getPos(), state.withProperty(RFMonitorBlock.OUTPUTPOWER, a), 2);
        worldObj.notifyNeighborsOfStateChange(this.pos, this.getBlockType());
        worldObj.markBlockForUpdate(this.pos);
    }


    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        if (tagCompound.hasKey("monitorX")) {
            monitor = new BlockPos(tagCompound.getInteger("monitorX"), tagCompound.getInteger("monitorY"), tagCompound.getInteger("monitorZ"));
        } else {
            monitor = null;
        }
        inAlarm = tagCompound.getBoolean("inAlarm");
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        rflevel = tagCompound.getInteger("rflevel");
        alarmMode = RFMonitorMode.getModeFromIndex(tagCompound.getByte("alarmMode"));
        alarmLevel = tagCompound.getByte("alarmLevel");
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        if (monitor != null) {
            tagCompound.setInteger("monitorX", monitor.getX());
            tagCompound.setInteger("monitorY", monitor.getY());
            tagCompound.setInteger("monitorZ", monitor.getZ());
        }
        tagCompound.setBoolean("inAlarm", inAlarm);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.setInteger("rflevel", getRflevel());
        tagCompound.setByte("alarmMode", (byte) alarmMode.getIndex());
        tagCompound.setByte("alarmLevel", (byte) alarmLevel);
    }

    @Override
    public List executeWithResultList(String command, Map<String, Argument> args) {
        List rc = super.executeWithResultList(command, args);
        if (rc != null) {
            return rc;
        }
        if (CMD_GETADJACENTBLOCKS.equals(command)) {
            return findAdjacentBlocks();
        }
        return null;
    }

    @Override
    public boolean execute(String command, List list) {
        boolean rc = super.execute(command, list);
        if (rc) {
            return true;
        }
        if (CLIENTCMD_ADJACENTBLOCKSREADY.equals(command)) {
            GuiRFMonitor.fromServer_clientAdjacentBlocks = (List<BlockPos>) list.stream().map(o -> ((BlockPosNet)o).getPos()).collect(Collectors.toList());
            return true;
        }
        return false;
    }
}
