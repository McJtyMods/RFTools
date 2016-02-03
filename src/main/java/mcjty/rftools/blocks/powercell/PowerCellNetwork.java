package mcjty.rftools.blocks.powercell;

import mcjty.lib.varia.GlobalCoordinate;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import java.util.*;

public class PowerCellNetwork extends WorldSavedData {

    public static final String POWERCELL_NETWORK_NAME = "RFToolsPowerCellNetwork";
    private static PowerCellNetwork instance = null;

    private int lastId = 0;

    private final Map<Integer,Network> networks = new HashMap<>();

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

        // Connectivity information that calculates the cost of extracting energy depending
        // on distance factors. Is recalculated automatically if it is null.
        private Map<GlobalCoordinate, Float> costFactor = null;

        public int getBlockCount() {
            return blocks.size();
        }

        public void add(GlobalCoordinate g) {
            if (!blocks.contains(g)) {
                blocks.add(g);
                costFactor = null;
            }
        }

        public void remove(GlobalCoordinate g) {
            if (blocks.contains(g)) {
                blocks.remove(g);
                costFactor = null;
            }
        }

        private double calculateBlobDistance(Set<GlobalCoordinate> blob1, Set<GlobalCoordinate> blob2) {
            // @todo, make rftools dimensions more efficient

            GlobalCoordinate c1 = blob1.iterator().next();
            GlobalCoordinate c2 = blob2.iterator().next();
            if (c1.getDimension() != c2.getDimension()) {
                return 10000.0;
            }
            double dist = Math.sqrt(c1.getCoordinate().distanceSq(c2.getCoordinate()));
            if (dist > 10000.0) {
                dist = 10000.0;
            }
            return dist;
        }

        private void updateCostFactor(World world) {
            if (costFactor == null) {
                costFactor = new HashMap<>();
                // Here we calculate the different blobs of powercells (all connected cells)
                List<Set<GlobalCoordinate>> blobs = new ArrayList<>();
                getBlobs(blobs);

                // For every blob we calculate it's 'strength' relative to the other blobs.
                for (Set<GlobalCoordinate> blob : blobs) {

                    double totalfactor = 1.0f;

                    // Scan all blobs different from this one
                    for (Set<GlobalCoordinate> blob2 : blobs) {
                        if (blob2 != blob) {
                            // The distance between the local blob and the other blob:
                            double dist = calculateBlobDistance(blob, blob2);

                            // 'part' is a number indicating how relevant this blob is for calculating
                            // the extraction cost. A big blob will have a big influence. If there is only
                            // one blob then this will be equal to 1.
                            double part = (double) blob2.size() / blocks.size();

                            // 'factor' indicates the cost of getting power out of blocks part of 'blob2'
                            // from the perspective of 'blob'.
                            double factor = 1 + (dist / 10000.0) * (PowerCellConfiguration.powerCellCostFactor - 1) * part;

                            totalfactor += factor;
                        }
                    }

                    // This is the average cost for getting power out of blocks from this blob:
                    totalfactor /= blobs.size();

                    // Set this to the coordinates of this blob
                    for (GlobalCoordinate coordinate : blob) {
                        costFactor.put(coordinate, (float) totalfactor);
                    }
                }
            }
        }
        private void getBlob(Set<GlobalCoordinate> todo, Set<GlobalCoordinate> blob, GlobalCoordinate coordinate) {
            blob.add(coordinate);
            for (EnumFacing facing : EnumFacing.values()) {
                GlobalCoordinate offset = new GlobalCoordinate(coordinate.getCoordinate().offset(facing), coordinate.getDimension());
                if (todo.contains(offset)) {
                    todo.remove(offset);
                    getBlob(todo, blob, offset);
                }
            }
        }

        // Get all sets of cells that are connected to each other.
        private void getBlobs(List<Set<GlobalCoordinate>> blobs) {
            Set<GlobalCoordinate> todo = new HashSet<>(blocks);
            while (!todo.isEmpty()) {
                GlobalCoordinate coordinate = todo.iterator().next();
                todo.remove(coordinate);
                Set<GlobalCoordinate> blob = new HashSet<>();
                getBlob(todo, blob, coordinate);
                blobs.add(blob);
            }
        }

        public float calculateCostFactor(World world, GlobalCoordinate g) {
            updateCostFactor(world);
            Float f = costFactor.get(g);
            return f == null ? 1.0f : f;
        }

        public int getEnergySingleBlock() {
            return energy / Math.max(1, blocks.size());
        }

        public int extractEnergySingleBlock() {
            int rc = energy / Math.max(1, blocks.size());
            energy -= rc;
            return rc;
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
                list.appendTag(tag);
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
