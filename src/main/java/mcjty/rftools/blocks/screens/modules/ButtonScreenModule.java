package mcjty.rftools.blocks.screens.modules;

import mcjty.rftools.api.screens.IScreenDataHelper;
import mcjty.rftools.api.screens.IScreenModule;
import mcjty.rftools.api.screens.data.IModuleDataBoolean;
import mcjty.rftools.blocks.logic.wireless.RedstoneChannels;
import mcjty.rftools.blocks.screens.ScreenConfiguration;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class ButtonScreenModule implements IScreenModule<IModuleDataBoolean> {
    private String line = "";
    private int channel = -1;
    private boolean toggle;

    @Override
    public IModuleDataBoolean getData(IScreenDataHelper helper, World worldObj, long millis) {
        if (channel != -1 && toggle) {
            RedstoneChannels channels = RedstoneChannels.getChannels(worldObj);
            RedstoneChannels.RedstoneChannel ch = channels.getOrCreateChannel(channel);
            return helper.createBoolean(ch.getValue() > 0);
        }
        return null;
    }

    @Override
    public void setupFromNBT(NBTTagCompound tagCompound, int dim, BlockPos pos) {
        if (tagCompound != null) {
            line = tagCompound.getString("text");
            if (tagCompound.hasKey("channel")) {
                channel = tagCompound.getInteger("channel");
            }
            toggle = tagCompound.getBoolean("toggle");
        }
    }

    @Override
    public void mouseClick(World world, int x, int y, boolean clicked, EntityPlayer player) {
        int xoffset;
        if (!line.isEmpty()) {
            xoffset = 80;
        } else {
            xoffset = 5;
        }
        if (x >= xoffset) {
            if (channel != -1) {
                if (toggle) {
                    if (clicked) {
                        RedstoneChannels channels = RedstoneChannels.getChannels(world);
                        RedstoneChannels.RedstoneChannel ch = channels.getOrCreateChannel(channel);
                        ch.setValue((ch.getValue() == 0) ? 15 : 0);
                        channels.save(world);
                    }
                } else {
                    RedstoneChannels channels = RedstoneChannels.getChannels(world);
                    RedstoneChannels.RedstoneChannel ch = channels.getOrCreateChannel(channel);
                    ch.setValue(clicked ? 15 : 0);
                    channels.save(world);
                }
            } else {
                if (player != null) {
                    player.sendStatusMessage(new TextComponentString(TextFormatting.RED + "Module is not linked to redstone channel!"), false);
                }
            }
        }
    }

    @Override
    public int getRfPerTick() {
        return ScreenConfiguration.BUTTON_RFPERTICK;
    }
}
