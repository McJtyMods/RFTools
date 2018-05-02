package mcjty.rftools.blocks.endergen;

import mcjty.lib.container.LogicTileEntity;
import mcjty.lib.gui.widgets.ChoiceLabel;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcjty.typed.TypedMap;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class EnderMonitorTileEntity extends LogicTileEntity implements ITickable {

    public static final String CMD_MODE = "endermonitor.setMode";

    private EnderMonitorMode mode = EnderMonitorMode.MODE_LOSTPEARL;

    private boolean needpulse = false;

    public EnderMonitorTileEntity() {
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
    public void update() {
        if (!getWorld().isRemote) {
            checkStateServer();
        }
    }

    private void checkStateServer() {
        int newout = 0;

        if (needpulse) {
            markDirtyQuick();
            newout = 15;
            needpulse = false;
        }

        setRedstoneState(newout);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        powerOutput = tagCompound.getBoolean("rs") ? 15 : 0;

        needpulse = tagCompound.getBoolean("needPulse");
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        int m = tagCompound.getInteger("mode");
        mode = EnderMonitorMode.values()[m];
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setBoolean("rs", powerOutput > 0);
        tagCompound.setBoolean("needPulse", needpulse);
        return tagCompound;
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.setInteger("mode", mode.ordinal());
    }

    @Override
    public boolean execute(EntityPlayerMP playerMP, String command, TypedMap params) {
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

    @Override
    @Optional.Method(modid = "theoneprobe")
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
        EnderMonitorMode m = getMode();
        probeInfo.text(TextFormatting.GREEN + "Mode: " + m.getDescription());
    }

    @SideOnly(Side.CLIENT)
    @Override
    @Optional.Method(modid = "waila")
    public void addWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.addWailaBody(itemStack, currenttip, accessor, config);
        EnderMonitorMode m = getMode();
        currenttip.add(TextFormatting.GREEN + "Mode: " + m.getDescription());
    }
}
