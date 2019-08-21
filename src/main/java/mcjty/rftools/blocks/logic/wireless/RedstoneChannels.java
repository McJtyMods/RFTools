package mcjty.rftools.blocks.logic.wireless;

import mcjty.lib.worlddata.AbstractWorldData;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.HashMap;
import java.util.Map;

public class RedstoneChannels extends AbstractWorldData<RedstoneChannels> {

    private static final String REDSTONE_CHANNELS_NAME = "RfToolsRedstoneChannels";

    private int lastId = 0;

    private final Map<Integer,RedstoneChannel> channels = new HashMap<>();

    public RedstoneChannels(String name) {
        super(name);
    }

    @Override
    public void clear() {
        channels.clear();
        lastId = 0;
    }

    public static RedstoneChannels getChannels(World world) {
        return getData(world, RedstoneChannels.class, REDSTONE_CHANNELS_NAME);
    }

    public RedstoneChannel getOrCreateChannel(int id) {
        RedstoneChannel channel = channels.get(id);
        if (channel == null) {
            channel = new RedstoneChannel();
            channels.put(id, channel);
        }
        return channel;
    }

    public RedstoneChannel getChannel(int id) {
        return channels.get(id);
    }

    public void deleteChannel(int id) {
        channels.remove(id);
    }

    public int newChannel() {
        lastId++;
        return lastId;
    }

    @Override
    public void readFromNBT(CompoundNBT tagCompound) {
        channels.clear();
        ListNBT lst = tagCompound.getTagList("channels", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < lst.tagCount() ; i++) {
            CompoundNBT tc = lst.getCompoundTagAt(i);
            int channel = tc.getInteger("channel");
            int v = tc.getInteger("value");

            RedstoneChannel value = new RedstoneChannel();
            value.value = v;
            channels.put(channel, value);
        }
        lastId = tagCompound.getInteger("lastId");
    }

    @Override
    public CompoundNBT writeToNBT(CompoundNBT tagCompound) {
        ListNBT lst = new ListNBT();
        for (Map.Entry<Integer, RedstoneChannel> entry : channels.entrySet()) {
            CompoundNBT tc = new CompoundNBT();
            tc.setInteger("channel", entry.getKey());
            tc.setInteger("value", entry.getValue().getValue());
            lst.appendTag(tc);
        }
        tagCompound.setTag("channels", lst);
        tagCompound.setInteger("lastId", lastId);
        return tagCompound;
    }

    public static class RedstoneChannel {
        private int value = 0;

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }
}
