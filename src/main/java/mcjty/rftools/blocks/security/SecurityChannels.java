package mcjty.rftools.blocks.security;

import mcjty.lib.worlddata.AbstractWorldData;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SecurityChannels extends AbstractWorldData<SecurityChannels> {

    private static final String SECURITY_CHANNELS_NAME = "RFToolsSecurityChannels";

    private int lastId = 0;

    private final Map<Integer,SecurityChannel> channels = new HashMap<>();

    public SecurityChannels(String name) {
        super(name);
    }

    @Override
    public void clear() {
        channels.clear();
        lastId = 0;
    }

    public static SecurityChannels getChannels(World world) {
        return getData(world, SecurityChannels.class, SECURITY_CHANNELS_NAME);
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
    public void readFromNBT(CompoundNBT tagCompound) {
        channels.clear();
        NBTTagList lst = tagCompound.getTagList("channels", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < lst.tagCount() ; i++) {
            CompoundNBT tc = lst.getCompoundTagAt(i);
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
    public CompoundNBT writeToNBT(CompoundNBT tagCompound) {
        NBTTagList lst = new NBTTagList();
        for (Map.Entry<Integer, SecurityChannel> entry : channels.entrySet()) {
            CompoundNBT tc = new CompoundNBT();
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
        return tagCompound;
    }

    public static class SecurityChannel {
        private String name = "";
        private boolean whitelist = true;
        private final List<String> players = new ArrayList<>();

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
