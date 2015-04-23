package mcjty.rftools.blocks.screens.modules;

import mcjty.rftools.blocks.logic.RedstoneChannels;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class ButtonScreenModule implements ScreenModule {
    private String line = "";
    private int channel = -1;
    private boolean toggle;

    public static final int RFPERTICK = 0;

    @Override
    public Object[] getData(World worldObj, long millis) {
        if (channel != -1 && toggle) {
            RedstoneChannels channels = RedstoneChannels.getChannels(worldObj);
            RedstoneChannels.RedstoneChannel ch = channels.getOrCreateChannel(channel);
            return new Object[] { ch.getValue()};
        }
        return null;
    }

    @Override
    public void setupFromNBT(NBTTagCompound tagCompound, int dim, int x, int y, int z) {
        if (tagCompound != null) {
            line = tagCompound.getString("text");
            if (tagCompound.hasKey("channel")) {
                channel = tagCompound.getInteger("channel");
            }
            toggle = tagCompound.getBoolean("toggle");
        }
    }

    @Override
    public void mouseClick(World world, int x, int y, boolean clicked) {
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
            }
        }
    }

    @Override
    public int getRfPerTick() {
        return RFPERTICK;
    }
}
