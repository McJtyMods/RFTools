package mcjty.rftools.blocks.endergen;

import mcjty.lib.gui.widgets.ChoiceLabel;
import mcjty.lib.tileentity.LogicTileEntity;
import mcjty.lib.typed.TypedMap;
import mcjty.rftools.TickOrderHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;

import static mcjty.rftools.blocks.endergen.EndergenicSetup.TYPE_ENDER_MONITOR;

public class EnderMonitorTileEntity extends LogicTileEntity implements ITickableTileEntity, TickOrderHandler.ICheckStateServer {

    public static final String CMD_MODE = "endermonitor.setMode";

    private EnderMonitorMode mode = EnderMonitorMode.MODE_LOSTPEARL;

    private boolean needpulse = false;

    public EnderMonitorTileEntity() {
        super(TYPE_ENDER_MONITOR);
    }

    public EnderMonitorMode getMode() {
        return mode;
    }

    public void setMode(EnderMonitorMode mode) {
        this.mode = mode;
        markDirtyClient();
    }

    /**
     * Callback from the endergenic in case something happens.
     * @param mode is the mode to fire
     */
    public void fireFromEndergenic(EnderMonitorMode mode) {
        if (this.mode != mode) {
            return; // Not monitoring this mode. We do nothing.
        }

        needpulse = true;
        markDirtyQuick();
    }

    @Override
    public void tick() {
        if (!world.isRemote) {
            TickOrderHandler.queueEnderMonitor(this);
        }
    }

    @Override
    public void checkStateServer() {
        int newout = 0;

        if (needpulse) {
            markDirtyQuick();
            newout = 15;
            needpulse = false;
        }

        setRedstoneState(newout);
    }

    @Override
    public int getDimension() {
        return world.getDimension().getType().getId();
    }

    @Override
    public void read(CompoundNBT tagCompound) {
        super.read(tagCompound);
        powerOutput = tagCompound.getBoolean("rs") ? 15 : 0;

        needpulse = tagCompound.getBoolean("needPulse");
        readRestorableFromNBT(tagCompound);
    }

    // @todo 1.14 loot tables
    public void readRestorableFromNBT(CompoundNBT tagCompound) {
        int m = tagCompound.getInt("mode");
        mode = EnderMonitorMode.values()[m];
    }

    @Override
    public CompoundNBT write(CompoundNBT tagCompound) {
        super.write(tagCompound);
        tagCompound.putBoolean("rs", powerOutput > 0);
        tagCompound.putBoolean("needPulse", needpulse);
        writeRestorableToNBT(tagCompound);
        return tagCompound;
    }

    // @todo 1.14 loot tables
    public void writeRestorableToNBT(CompoundNBT tagCompound) {
        tagCompound.putInt("mode", mode.ordinal());
    }

    @Override
    public boolean execute(PlayerEntity playerMP, String command, TypedMap params) {
        boolean rc = super.execute(playerMP, command, params);
        if (rc) {
            return true;
        }
        if (CMD_MODE.equals(command)) {
            String m = params.get(ChoiceLabel.PARAM_CHOICE);
            setMode(EnderMonitorMode.getMode(m));
            return true;
        }
        return false;
    }

//    @Override
//    @Optional.Method(modid = "theoneprobe")
//    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
//        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
//        EnderMonitorMode m = getMode();
//        probeInfo.text(TextFormatting.GREEN + "Mode: " + m.getDescription());
//    }
//
//    @SideOnly(Side.CLIENT)
//    @Override
//    @Optional.Method(modid = "waila")
//    public void addWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
//        super.addWailaBody(itemStack, currenttip, accessor, config);
//        EnderMonitorMode m = getMode();
//        currenttip.add(TextFormatting.GREEN + "Mode: " + m.getDescription());
//    }
}
