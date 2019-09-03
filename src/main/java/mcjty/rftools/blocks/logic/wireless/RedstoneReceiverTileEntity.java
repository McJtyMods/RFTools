package mcjty.rftools.blocks.logic.wireless;

import mcjty.lib.gui.widgets.ToggleButton;
import mcjty.lib.typed.TypedMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;

import static mcjty.rftools.blocks.logic.LogicBlockSetup.TYPE_REDSTONE_RECEIVER;

public class RedstoneReceiverTileEntity extends RedstoneChannelTileEntity implements ITickableTileEntity {

    public static final String CMD_SETANALOG = "receiver.setAnalog";

    private boolean analog = false;

    public RedstoneReceiverTileEntity() {
        super(TYPE_REDSTONE_RECEIVER);
    }

    public boolean getAnalog() {
        return analog;
    }

    public void setAnalog(boolean analog) {
        this.analog = analog;
        markDirtyClient();
    }

    @Override
    public void tick() {
        if (!world.isRemote) {
            checkStateServer();
        }
    }

    protected void checkStateServer() {
        setRedstoneState(checkOutput());
    }

    public int checkOutput() {
        if (channel != -1) {
            RedstoneChannels channels = RedstoneChannels.get();
            RedstoneChannels.RedstoneChannel ch = channels.getChannel(channel);
            if (ch != null) {
                int newout = ch.getValue();
                if(!analog && newout > 0) {
                    return 15;
                }
                return newout;
            }
        }
        return 0;
    }

    @Override
    public void read(CompoundNBT tagCompound) {
        super.read(tagCompound);
        if(tagCompound.contains("rs", 3 /* int */)) {
            powerOutput = tagCompound.getInt("rs");
        } else {
            powerOutput = tagCompound.getBoolean("rs") ? 15 : 0; // backwards compatibility
        }
        readRestorableFromNBT(tagCompound);
    }

    @Override
    public CompoundNBT write(CompoundNBT tagCompound) {
        super.write(tagCompound);
        tagCompound.putInt("rs", powerOutput);
        writeRestorableToNBT(tagCompound);
        return tagCompound;
    }

    // @todo 1.14 loot tables
    public void readRestorableFromNBT(CompoundNBT tagCompound) {
        analog = tagCompound.getBoolean("analog");
    }

    // @todo 1.14 loot tables
    public void writeRestorableToNBT(CompoundNBT tagCompound) {
        tagCompound.putBoolean("analog", analog);
    }

    @Override
    public boolean execute(PlayerEntity playerMP, String command, TypedMap params) {
        boolean rc = super.execute(playerMP, command, params);
        if (rc) {
            return true;
        }
        if (CMD_SETANALOG.equals(command)) {
            setAnalog(params.get(ToggleButton.PARAM_ON));
            return true;
        }
        return false;
    }

//    @Override
//    @Optional.Method(modid = "theoneprobe")
//    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, BlockState blockState, IProbeHitData data) {
//        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
//        probeInfo.text(TextFormatting.GREEN + "Analog mode: " + getAnalog());
//        probeInfo.text(TextFormatting.GREEN + "Output: " + TextFormatting.WHITE + checkOutput());
//    }
//
//    @SideOnly(Side.CLIENT)
//    @Override
//    @Optional.Method(modid = "waila")
//    public void addWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
//        super.addWailaBody(itemStack, currenttip, accessor, config);
//        currenttip.add(TextFormatting.GREEN + "Analog mode: " + getAnalog());
//    }

}
