package mcjty.rftools.blocks.powercell;

import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.GlobalCoordinate;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PowerCellNetwork extends WorldSavedData {

    public static final String POWERCELL_NETWORK_NAME = "RFToolsPowerCellNetwork";
    private static PowerCellNetwork instance = null;

    private int lastId = 0;

    private final Map<Integer,Network> networks = new HashMap<Integer,Network>();

    public PowerCellNetwork(String identifier) {
        super(identifier);
    }

    public void save(World world) {
        world.setItemData(POWERCELL_NETWORK_NAME, this);
        markDirty();
    }

    public static void clearInstance() {
        if (instance != null) {
            instance.networks.clear();
            instance = null;
        }
    }

    public static PowerCellNetwork getChannels() {
        return instance;
    }

    public static PowerCellNetwork getChannels(World world) {
        if (world.isRemote) {
            return null;
        }
        if (instance != null) {
            return instance;
        }
        instance = (PowerCellNetwork) world.loadItemData(PowerCellNetwork.class, POWERCELL_NETWORK_NAME);
        if (instance == null) {
            instance = new PowerCellNetwork(POWERCELL_NETWORK_NAME);
        }
        return instance;
    }

    public Network getOrCreateNetwork(int id) {
        Network channel = networks.get(id);
        if (channel == null) {
            channel = new Network();
            networks.put(id, channel);
        }
        return channel;
    }

    public Network getChannel(int id) {
        return networks.get(id);
    }

    public void deleteChannel(int id) {
        networks.remove(id);
    }

    public int newChannel() {
        lastId++;
        return lastId;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        networks.clear();
        NBTTagList lst = tagCompound.getTagList("networks", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < lst.tagCount() ; i++) {
            NBTTagCompound tc = lst.getCompoundTagAt(i);
            int channel = tc.getInteger("channel");
            Network value = new Network();
            value.readFromNBT(tc);
            networks.put(channel, value);
        }
        lastId = tagCompound.getInteger("lastId");
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        NBTTagList lst = new NBTTagList();
        for (Map.Entry<Integer, Network> entry : networks.entrySet()) {
            NBTTagCompound tc = new NBTTagCompound();
            tc.setInteger("channel", entry.getKey());
            entry.getValue().writeToNBT(tc);
            lst.appendTag(tc);
        }
        tagCompound.setTag("networks", lst);
        tagCompound.setInteger("lastId", lastId);
    }

    public static class Network {
        private int energy = 0;
        private Set<GlobalCoordinate> blocks = new HashSet<>();

        public Set<GlobalCoordinate> getBlocks() {
            return blocks;
        }

        public int getEnergy() {
            return energy;
        }

        public void setEnergy(int energy) {
            this.energy = energy;
        }

        public void writeToNBT(NBTTagCompound tagCompound){
            tagCompound.setInteger("energy", energy);
            NBTTagList list = new NBTTagList();
            for (GlobalCoordinate block : blocks) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setInteger("dim", block.getDimension());
                tag.setInteger("x", block.getCoordinate().getX());
                tag.setInteger("y", block.getCoordinate().getY());
                tag.setInteger("z", block.getCoordinate().getZ());
            }

            tagCompound.setTag("blocks", list);
        }

        public void readFromNBT(NBTTagCompound tagCompound){
            this.energy = tagCompound.getInteger("energy");
            blocks.clear();
            NBTTagList list = tagCompound.getTagList("blocks", Constants.NBT.TAG_COMPOUND);
            for (int i = 0 ; i < list.tagCount() ; i++) {
                NBTTagCompound tag = list.getCompoundTagAt(i);
                blocks.add(new GlobalCoordinate(new BlockPos(tag.getInteger("x"), tag.getInteger("y"), tag.getInteger("z")), tag.getInteger("dim")));
            }
        }
    }
}
