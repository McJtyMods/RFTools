package mcjty.rftools.blocks.builder;

import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.worlddata.AbstractWorldData;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.util.Constants;

import java.util.HashMap;
import java.util.Map;

public class SpaceChamberRepository extends AbstractWorldData<SpaceChamberRepository> {

    private static final String SPACECHAMBER_CHANNELS_NAME = "RFToolsSpaceChambers";

    private int lastId = 0;

    private final Map<Integer,SpaceChamberChannel> channels = new HashMap<>();

    public SpaceChamberRepository() {
        super(SPACECHAMBER_CHANNELS_NAME);
    }

    public static SpaceChamberRepository get() {
        return getData(SpaceChamberRepository::new, SPACECHAMBER_CHANNELS_NAME);
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
    public void read(CompoundNBT tagCompound) {
        channels.clear();
        ListNBT lst = tagCompound.getList("channels", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < lst.size() ; i++) {
            CompoundNBT tc = lst.getCompound(i);
            int channel = tc.getInt("channel");

            SpaceChamberChannel value = new SpaceChamberChannel();
            value.setDimension(DimensionType.byName(new ResourceLocation(tc.getString("dimension"))));
            value.setMinCorner(BlockPosTools.read(tc, "minCorner"));
            value.setMaxCorner(BlockPosTools.read(tc, "maxCorner"));
            channels.put(channel, value);
        }
        lastId = tagCompound.getInt("lastId");
    }

    @Override
    public CompoundNBT write(CompoundNBT tagCompound) {
        ListNBT lst = new ListNBT();
        for (Map.Entry<Integer, SpaceChamberChannel> entry : channels.entrySet()) {
            CompoundNBT tc = new CompoundNBT();
            tc.putInt("channel", entry.getKey());
            tc.putString("dimension", entry.getValue().getDimension().getRegistryName().toString());
            BlockPosTools.write(tc, "minCorner", entry.getValue().getMinCorner());
            BlockPosTools.write(tc, "maxCorner", entry.getValue().getMaxCorner());
            lst.add(tc);
        }
        tagCompound.put("channels", lst);
        tagCompound.putInt("lastId", lastId);
        return tagCompound;
    }

    public static class SpaceChamberChannel {
        private DimensionType dimension;
        private BlockPos minCorner = null;
        private BlockPos maxCorner = null;

        public DimensionType getDimension() {
            return dimension;
        }

        public void setDimension(DimensionType dimension) {
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
