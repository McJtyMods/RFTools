package mcjty.rftools.blocks.monitor;

import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.CapabilityTools;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static mcjty.rftools.blocks.monitor.MonitorSetup.TYPE_LIQUID_MONITOR;

public class LiquidMonitorBlockTileEntity extends GenericTileEntity implements ITickableTileEntity {
    // Data that is saved
    private BlockPos monitor;
    private RFMonitorMode alarmMode = RFMonitorMode.MODE_OFF;
    private int alarmLevel = 0;             // The level (in percentage) at which we give an alarm

    public static IntegerProperty LEVEL = IntegerProperty.create("level", 0, 5);

    public static final String CMD_GETADJACENTBLOCKS = "getAdj";
    public static final String CLIENTCMD_ADJACENTBLOCKSREADY = "adjReady";

    // Temporary data
    private int counter = 20;

    private int fluidlevel = 0;
    private boolean inAlarm = false;

    public LiquidMonitorBlockTileEntity() {
        super(TYPE_LIQUID_MONITOR);
    }

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
            if (yy >= 0 && yy < getWorld().getHeight()) {
                for (int dz = -1 ; dz <= 1 ; dz++) {
                    int zz = z + dz;
                    for (int dx = -1 ; dx <= 1 ; dx++) {
                        int xx = x + dx;
                        if (dx != 0 || dy != 0 || dz != 0) {
                            TileEntity tileEntity = getWorld().getTileEntity(new BlockPos(xx, yy, zz));
                            if (tileEntity != null) {
                                tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).ifPresent(h -> {
                                    adjacentBlocks.add(new BlockPos(xx, yy, zz));
                                });
                            }
                        }
                    }
                }
            }
        }
        return adjacentBlocks;
    }

    @Override
    public void tick() {
        if (!world.isRemote) {
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

        AtomicLong stored = new AtomicLong();
        AtomicLong maxContents = new AtomicLong();

        TileEntity tileEntity = world.getTileEntity(monitor);
        if (!CapabilityTools.getFluidCapabilitySafe(tileEntity).map(fluidHandler -> {
            IFluidTankProperties[] properties = fluidHandler.getTankProperties();
            if (properties != null && properties.length > 0) {
                if (properties[0].getContents() != null) {
                    stored.set(properties[0].getContents().amount);
                }
                maxContents.set(properties[0].getCapacity());
            }
            return true;
        }).orElseGet(() -> {
            setInvalid();
            return false;
        })) {
            return;
        };

        int ratio = 0;  // Will be set as metadata;
        boolean alarm = false;

        if (maxContents.get() > 0) {
            ratio = (int) (1 + (stored.get() * 5) / maxContents.get());
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
                    alarm = ((stored.get() * 100 / maxContents.get()) < alarmLevel);
                    break;
                case MODE_MORE:
                    alarm = ((stored.get() * 100 / maxContents.get()) > alarmLevel);
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
        getWorld().notifyNeighborsOfStateChange(this.pos, MonitorSetup.liquidMonitorBlock);
    }

    @Override
    public void read(CompoundNBT tagCompound) {
        super.read(tagCompound);
        if (tagCompound.contains("monitorX")) {
            monitor = new BlockPos(tagCompound.getInt("monitorX"), tagCompound.getInt("monitorY"), tagCompound.getInt("monitorZ"));
        } else {
            monitor = null;
        }
        inAlarm = tagCompound.getBoolean("inAlarm");
        // @todo 1.14 loot tables
        readRestorableFromNBT(tagCompound);
    }

    public void readRestorableFromNBT(CompoundNBT tagCompound) {
        fluidlevel = tagCompound.getInt("fluidlevel");
        alarmMode = RFMonitorMode.getModeFromIndex(tagCompound.getByte("alarmMode"));
        alarmLevel = tagCompound.getByte("alarmLevel");
    }

    @Override
    public CompoundNBT write(CompoundNBT tagCompound) {
        super.write(tagCompound);
        if (monitor != null) {
            tagCompound.putInt("monitorX", monitor.getX());
            tagCompound.putInt("monitorY", monitor.getY());
            tagCompound.putInt("monitorZ", monitor.getZ());
        }
        tagCompound.putBoolean("inAlarm", inAlarm);
        writeRestorableToNBT(tagCompound);
        return tagCompound;
    }

    // @todo 1.14 loot tables
    public void writeRestorableToNBT(CompoundNBT tagCompound) {
        tagCompound.putInt("fluidlevel", getFluidLevel());
        tagCompound.putByte("alarmMode", (byte) alarmMode.getIndex());
        tagCompound.putByte("alarmLevel", (byte) alarmLevel);
    }

    @Nonnull
    @Override
    public <T> List<T> executeWithResultList(String command, TypedMap args, Type<T> type) {
        List<T> rc = super.executeWithResultList(command, args, type);
        if (!rc.isEmpty()) {
            return rc;
        }
        if (CMD_GETADJACENTBLOCKS.equals(command)) {
            return type.convert(findAdjacentBlocks());
        }
        return Collections.emptyList();
    }

    @Override
    public <T> boolean receiveListFromServer(String command, List<T> list, Type<T> type) {
        boolean rc = super.receiveListFromServer(command, list, type);
        if (rc) {
            return true;
        }
        if (CLIENTCMD_ADJACENTBLOCKSREADY.equals(command)) {
            GuiLiquidMonitor.fromServer_clientAdjacentBlocks = new ArrayList<>(Type.create(BlockPos.class).convert(list));
            return true;
        }
        return false;
    }

// @todo 1.14 proper blockstate
//    @Override
//    public BlockState getActualState(BlockState state) {
//        int level = getFluidLevel();
//        return state.withProperty(LEVEL, level);
//    }


    @Override
    public int getRedstoneOutput(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
        Direction direction = state.get(BlockStateProperties.FACING);
        if (side == direction) {
            return isPowered() ? 15 : 0;
        }
        return 0;
    }
}
