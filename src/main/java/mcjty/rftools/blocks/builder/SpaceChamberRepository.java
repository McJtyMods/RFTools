package mcjty.rftools.blocks.builder;

import mcjty.lib.varia.BlockPosTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import java.util.HashMap;
import java.util.Map;

public class SpaceChamberRepository extends WorldSavedData {

    public static final String SPACECHAMBER_CHANNELS_NAME = "RFToolsSpaceChambers";
    private static SpaceChamberRepository instance = null;

    private int lastId = 0;

    private final Map<Integer,SpaceChamberChannel> channels = new HashMap<Integer,SpaceChamberChannel>();

    public SpaceChamberRepository(String identifier) {
        super(identifier);
    }

    public void save(World world) {
        world.setData(SPACECHAMBER_CHANNELS_NAME, this);
        markDirty();
    }

    public static void clearInstance() {
        if (instance != null) {
            instance.channels.clear();
            instance = null;
        }
    }

    public static SpaceChamberRepository getChannels() {
        return instance;
    }

    public static SpaceChamberRepository getChannels(World world) {
        if (world.isRemote) {
            return null;
        }
        if (instance != null) {
            return instance;
        }
        instance = (SpaceChamberRepository) world.loadData(SpaceChamberRepository.class, SPACECHAMBER_CHANNELS_NAME);
        if (instance == null) {
            instance = new SpaceChamberRepository(SPACECHAMBER_CHANNELS_NAME);
        }
        return instance;
    }

    public SpaceChamberChannel getOrCreateChannel(int id) {
        SpaceChamberChannel channel = channels.get(id);
        if (channel == null) {
            channel = new SpaceChamberChannel();
            channels.put(id, channel);
        }
        return channel;
    }

    public SpaceChamberChannel getChannel(int id) {
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

            SpaceChamberChannel value = new SpaceChamberChannel();
            value.setDimension(tc.getInteger("dimension"));
            value.setMinCorner(BlockPosTools.readFromNBT(tc, "minCorner"));
            value.setMaxCorner(BlockPosTools.readFromNBT(tc, "maxCorner"));
            channels.put(channel, value);
        }
        lastId = tagCompound.getInteger("lastId");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        NBTTagList lst = new NBTTagList();
        for (Map.Entry<Integer, SpaceChamberChannel> entry : channels.entrySet()) {
            NBTTagCompound tc = new NBTTagCompound();
            tc.setInteger("channel", entry.getKey());
            tc.setInteger("dimension", entry.getValue().getDimension());
            BlockPosTools.writeToNBT(tc, "minCorner", entry.getValue().getMinCorner());
            BlockPosTools.writeToNBT(tc, "maxCorner", entry.getValue().getMaxCorner());
            lst.appendTag(tc);
        }
        tagCompound.setTag("channels", lst);
        tagCompound.setInteger("lastId", lastId);
        return tagCompound;
    }

    public static class SpaceChamberChannel {
        private int dimension;
        private BlockPos minCorner = null;
        private BlockPos maxCorner = null;

        public int getDimension() {
            return dimension;
        }

        public void setDimension(int dimension) {
            this.dimension = dimension;
        }

        public BlockPos getMinCorner() {
            return minCorner;
        }

        public void setMinCorner(BlockPos minCorner) {
            this.minCorner = minCorner;
        }

        public BlockPos getMaxCorner() {
            return maxCorner;
        }

        public void setMaxCorner(BlockPos maxCorner) {
            this.maxCorner = maxCorner;
        }
    }
}
