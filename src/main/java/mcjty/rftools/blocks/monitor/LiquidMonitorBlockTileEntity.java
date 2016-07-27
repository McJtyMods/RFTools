package mcjty.rftools.blocks.monitor;

import mcjty.lib.entity.GenericTileEntity;
import mcjty.lib.network.Argument;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LiquidMonitorBlockTileEntity extends GenericTileEntity implements ITickable {
    // Data that is saved
    private BlockPos monitor;
    private RFMonitorMode alarmMode = RFMonitorMode.MODE_OFF;
    private int alarmLevel = 0;             // The level (in percentage) at which we give an alarm

    public static final String CMD_GETADJACENTBLOCKS = "getAdj";
    public static final String CLIENTCMD_ADJACENTBLOCKSREADY = "adjReady";

    // Temporary data
    private int counter = 20;

    private int fluidlevel = 0;
    private boolean inAlarm = false;

    public RFMonitorMode getAlarmMode() {
        return alarmMode;
    }

    public int getAlarmLevel() {
        return alarmLevel;
    }

    public boolean isPowered() {
        return inAlarm;
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
        if (fluidlevel != 0) {
            fluidlevel = 0;
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

    public int getFluidLevel() {
        return fluidlevel;
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
                            if (tileEntity instanceof IFluidHandler) {
                                adjacentBlocks.add(new BlockPos(xx, yy, zz));
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
        if (!(tileEntity instanceof IFluidHandler)) {
            setInvalid();
            return;
        }
        IFluidHandler handler = (IFluidHandler) tileEntity;
        FluidTankInfo[] tankInfo = handler.getTankInfo(EnumFacing.DOWN);
        long stored = 0;
        long maxContents = 0;
        if (tankInfo != null && tankInfo.length > 0) {
            if (tankInfo[0].fluid != null) {
                stored = tankInfo[0].fluid.amount;
            }
            maxContents = tankInfo[0].capacity;
        }

        int ratio = 0;  // Will be set as metadata;
        boolean alarm = false;

        if (maxContents > 0) {
            ratio = (int) (1 + (stored * 5) / maxContents);
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
                    alarm = ((stored * 100 / maxContents) < alarmLevel);
                    break;
                case MODE_MORE:
                    alarm = ((stored * 100 / maxContents) > alarmLevel);
                    break;
            }

        }
        if (fluidlevel != ratio) {
            fluidlevel = ratio;
            markDirtyClient();
        }
        if (alarm != inAlarm) {
            inAlarm = alarm;
            setRedstoneOut(inAlarm);
            markDirty();
        }
    }

    private void setRedstoneOut(boolean a) {
        worldObj.notifyNeighborsOfStateChange(this.pos, this.getBlockType());
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
        fluidlevel = tagCompound.getInteger("fluidlevel");
        alarmMode = RFMonitorMode.getModeFromIndex(tagCompound.getByte("alarmMode"));
        alarmLevel = tagCompound.getByte("alarmLevel");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        if (monitor != null) {
            tagCompound.setInteger("monitorX", monitor.getX());
            tagCompound.setInteger("monitorY", monitor.getY());
            tagCompound.setInteger("monitorZ", monitor.getZ());
        }
        tagCompound.setBoolean("inAlarm", inAlarm);
        return tagCompound;
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.setInteger("fluidlevel", getFluidLevel());
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
            GuiLiquidMonitor.fromServer_clientAdjacentBlocks = (List<BlockPos>) list.stream().map(o -> ((BlockPosNet)o).getPos()).collect(Collectors.toList());
            return true;
        }
        return false;
    }
}
