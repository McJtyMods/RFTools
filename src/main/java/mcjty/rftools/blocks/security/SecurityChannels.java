package mcjty.rftools.blocks.security;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
            instance.channels.clear();
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
            value.setWhitelist(tc.getBoolean("whitelist"));

            value.clearPlayers();
            NBTTagList playerList = tc.getTagList("players", Constants.NBT.TAG_STRING);
            if (playerList != null) {
                for (int j = 0 ; j < playerList.tagCount() ; j++) {
                    String player = playerList.getStringTagAt(j);
                    value.addPlayer(player);
                }
            }

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
            SecurityChannel channel = entry.getValue();
            tc.setString("name", channel.getName());
            tc.setBoolean("whitelist", channel.isWhitelist());

            NBTTagList playerTagList = new NBTTagList();
            for (String player : channel.getPlayers()) {
                playerTagList.appendTag(new NBTTagString(player));
            }
            tc.setTag("players", playerTagList);

            lst.appendTag(tc);
        }
        tagCompound.setTag("channels", lst);
        tagCompound.setInteger("lastId", lastId);
    }

    public static class SecurityChannel {
        private String name = "";
        private boolean whitelist = true;
        private final List<String> players = new ArrayList<String>();

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<String> getPlayers() {
            return players;
        }

        public void addPlayer(String player) {
            players.add(player);
        }

        public void delPlayer(String player) {
            players.remove(player);
        }

        public void clearPlayers() {
            players.clear();
        }

        public boolean isWhitelist() {
            return whitelist;
        }

        public void setWhitelist(boolean whitelist) {
            this.whitelist = whitelist;
        }
    }
}
