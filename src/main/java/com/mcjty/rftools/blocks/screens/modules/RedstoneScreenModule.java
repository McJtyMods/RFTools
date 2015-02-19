package com.mcjty.rftools.blocks.screens.modules;

import com.mcjty.rftools.blocks.logic.RedstoneChannels;
import net.minecraft.nbt.NBTTagCompound;

public class RedstoneScreenModule implements ScreenModule {
    public static final int RFPERTICK = 4;
    protected int channel = -1;

    @Override
    public Object[] getData(long millis) {
        if (channel == -1) {
            return null;
        }
        RedstoneChannels channels = RedstoneChannels.getChannels();
        if (channels == null) {
            return null;
        }
        RedstoneChannels.RedstoneChannel ch = channels.getChannel(channel);
        if (ch == null) {
            return null;
        }
        return new Object[] { ch.getValue() != 0 ? true : false };
    }

    @Override
    public void setupFromNBT(NBTTagCompound tagCompound, int dim, int x, int y, int z) {
        if (tagCompound != null) {
            channel = -1;
            if (tagCompound.hasKey("channel")) {
                channel = tagCompound.getInteger("channel");
            }
        }
    }

    @Override
    public int getRfPerTick() {
        return RFPERTICK;
    }
}
