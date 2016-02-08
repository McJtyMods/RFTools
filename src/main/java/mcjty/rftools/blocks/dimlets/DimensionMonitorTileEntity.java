package mcjty.rftools.blocks.dimlets;

import mcjty.lib.entity.GenericTileEntity;
import mcjty.lib.network.Argument;
import mcjty.lib.varia.BlockTools;
import mcjty.rftools.dimension.DimensionStorage;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Map;

public class DimensionMonitorTileEntity extends GenericTileEntity {

    public static final String CMD_SETALARM = "setAlarm";

    private int alarmLevel = 0;
    private int ticker = 10;
    private boolean redstoneOut = false;

    public DimensionMonitorTileEntity() {
    }

    public int getAlarmLevel() {
        return alarmLevel;
    }

    public void setAlarmLevel(int level) {
        this.alarmLevel = level;
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    @Override
    protected void checkStateServer() {
        super.checkStateServer();

        ticker--;
        if (ticker > 0) {
            return;
        }
        ticker = 10;

        DimensionStorage storage = DimensionStorage.getDimensionStorage(worldObj);
        int energy = storage.getEnergyLevel(worldObj.provider.dimensionId);

        int pct = energy / (DimletConfiguration.MAX_DIMENSION_POWER / 100);
        boolean newout = pct < alarmLevel;

        if (newout != redstoneOut) {
            redstoneOut = newout;
            notifyBlockUpdate();
        }
    }

    @Override
    protected int updateMetaData(int meta) {
        meta = super.updateMetaData(meta);
        return BlockTools.setRedstoneSignalOut(meta, redstoneOut);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        redstoneOut = tagCompound.getBoolean("rs");
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        alarmLevel = tagCompound.getInteger("level");
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setBoolean("rs", redstoneOut);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.setInteger("level", alarmLevel);
    }

    @Override
    public boolean execute(EntityPlayerMP playerMP, String command, Map<String, Argument> args) {
        boolean rc = super.execute(playerMP, command, args);
        if (rc) {
            return true;
        }
        if (CMD_SETALARM.equals(command)) {
            setAlarmLevel(args.get("level").getInteger());
            return true;
        }
        return false;
    }
}
