package mcjty.rftools.blocks.security;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import java.util.HashMap;
import java.util.Map;

public class SecurityChannels extends WorldSavedData {

    public static final String SECURITY_CHANNELS_NAME = "RFToolsSecurityChannels";
    private static SecurityChannels instance = null;

    private int lastId = 0;

    private final Map<Integer,SecurityChannel> channels = new HashMap<Integer,SecurityChannel>();

    public SecurityChannels(String identifier) {
        super(identifier);
    }

    public void save(World world) {
        world.mapStorage.setData(SECURITY_CHANNELS_NAME, this);
        markDirty();
    }

    public static void clearInstance() {
        if (instance != null) {
//            instance.channels.clear();
            instance = null;
        }
    }

    public static SecurityChannels getChannels() {
        return instance;
    }

    public static SecurityChannels getChannels(World world) {
        if (world.isRemote) {
            return null;
        }
        if (instance != null) {
            return instance;
        }
        instance = (SecurityChannels) world.mapStorage.loadData(SecurityChannels.class, SECURITY_CHANNELS_NAME);
        if (instance == null) {
            instance = new SecurityChannels(SECURITY_CHANNELS_NAME);
        }
        return instance;
    }

    public SecurityChannel getOrCreateChannel(int id) {
        SecurityChannel channel = channels.get(id);
        if (channel == null) {
            channel = new SecurityChannel();
            channels.put(id, channel);
        }
        return channel;
    }

    public SecurityChannel getChannel(int id) {
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

            SecurityChannel value = new SecurityChannel();
            value.setName(tc.getString("name"));
            channels.put(channel, value);
        }
        lastId = tagCompound.getInteger("lastId");
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        NBTTagList lst = new NBTTagList();
        for (Map.Entry<Integer, SecurityChannel> entry : channels.entrySet()) {
            NBTTagCompound tc = new NBTTagCompound();
            tc.setInteger("channel", entry.getKey());
            tc.setString("name", entry.getValue().getName());
            lst.appendTag(tc);
        }
        tagCompound.setTag("channels", lst);
        tagCompound.setInteger("lastId", lastId);
    }

    public static class SecurityChannel {
        private String name = "";

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
