package mcjty.rftools.blocks.logic;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import java.util.HashMap;
import java.util.Map;

public class RedstoneChannels extends WorldSavedData {

    public static final String REDSTONE_CHANNELS_NAME = "RfToolsRedstoneChannels";
    private static RedstoneChannels instance = null;

    private int lastId = 0;

    private final Map<Integer,RedstoneChannel> channels = new HashMap<Integer,RedstoneChannel>();

    public RedstoneChannels(String identifier) {
        super(identifier);
    }

    public void save(World world) {
        world.getMapStorage().setData(REDSTONE_CHANNELS_NAME, this);
        markDirty();
    }

    public static void clearInstance() {
        if (instance != null) {
            instance.channels.clear();
            instance = null;
        }
    }

    public static RedstoneChannels getChannels() {
        return instance;
    }

    public static RedstoneChannels getChannels(World world) {
        if (world.isRemote) {
            return null;
        }
        if (instance != null) {
            return instance;
        }
        instance = (RedstoneChannels) world.getMapStorage().getOrLoadData(RedstoneChannels.class, REDSTONE_CHANNELS_NAME);
        if (instance == null) {
            instance = new RedstoneChannels(REDSTONE_CHANNELS_NAME);
        }
        return instance;
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
    public void readFromNBT(NBTTagCompound tagCompound) {
        channels.clear();
        NBTTagList lst = tagCompound.getTagList("channels", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < lst.tagCount() ; i++) {
            NBTTagCompound tc = lst.getCompoundTagAt(i);
            int channel = tc.getInteger("channel");
            int v = tc.getInteger("value");

            RedstoneChannel value = new RedstoneChannel();
            value.value = v;
            channels.put(channel, value);
        }
        lastId = tagCompound.getInteger("lastId");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        NBTTagList lst = new NBTTagList();
        for (Map.Entry<Integer, RedstoneChannel> entry : channels.entrySet()) {
            NBTTagCompound tc = new NBTTagCompound();
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
