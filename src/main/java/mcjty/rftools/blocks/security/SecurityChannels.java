package mcjty.rftools.blocks.security;

import mcjty.lib.worlddata.AbstractWorldData;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
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

    public SecurityChannels() {
        super(SECURITY_CHANNELS_NAME);
    }

    public static SecurityChannels get() {
        return getData(SecurityChannels::new, SECURITY_CHANNELS_NAME);
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
    public void read(CompoundNBT tagCompound) {
        channels.clear();
        ListNBT lst = tagCompound.getList("channels", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < lst.size() ; i++) {
            CompoundNBT tc = lst.getCompound(i);
            int channel = tc.getInt("channel");

            SecurityChannel value = new SecurityChannel();
            value.setName(tc.getString("name"));
            value.setWhitelist(tc.getBoolean("whitelist"));

            value.clearPlayers();
            ListNBT playerList = tc.getList("players", Constants.NBT.TAG_STRING);
            if (playerList != null) {
                for (int j = 0 ; j < playerList.size() ; j++) {
                    String player = playerList.getString(j);
                    value.addPlayer(player);
                }
            }

            channels.put(channel, value);
        }
        lastId = tagCompound.getInt("lastId");
    }

    @Override
    public CompoundNBT write(CompoundNBT tagCompound) {
        ListNBT lst = new ListNBT();
        for (Map.Entry<Integer, SecurityChannel> entry : channels.entrySet()) {
            CompoundNBT tc = new CompoundNBT();
            tc.putInt("channel", entry.getKey());
            SecurityChannel channel = entry.getValue();
            tc.putString("name", channel.getName());
            tc.putBoolean("whitelist", channel.isWhitelist());

            ListNBT playerTagList = new ListNBT();
            for (String player : channel.getPlayers()) {
                playerTagList.add(new StringNBT(player));
            }
            tc.put("players", playerTagList);

            lst.add(tc);
        }
        tagCompound.put("channels", lst);
        tagCompound.putInt("lastId", lastId);
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
