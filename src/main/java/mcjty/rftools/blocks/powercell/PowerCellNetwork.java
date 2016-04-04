package mcjty.rftools.blocks.powercell;

import mcjty.lib.varia.GlobalCoordinate;
import mcjty.lib.varia.Logging;
import mcjty.rftools.RFTools;
import mcjty.rftools.apideps.RFToolsDimensionChecker;
import mcjty.rftools.blocks.teleporter.TeleportationTools;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
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
        private int advancedBlocks = 0;

        // Connectivity information that calculates the cost of extracting energy depending
        // on distance factors. Is recalculated automatically if it is null.
        private Map<GlobalCoordinate, Float> costFactor = null;

        // Be careful with this! Don't modify the set
        public Set<GlobalCoordinate> getBlocks() {
            return blocks;
        }

        public int getBlockCount() {
            return blocks.size();
        }

        public int getAdvancedBlockCount() {
            return advancedBlocks;
        }

        public void updateNetwork(World w) {
            advancedBlocks = 0;
            Iterable<GlobalCoordinate> copy = new HashSet<GlobalCoordinate>(blocks);
            blocks.clear();
            for (GlobalCoordinate c : copy) {
                World world = TeleportationTools.getWorldForDimension(w, c.getDimension());
                IBlockState state = world.getBlockState(c.getCoordinate());
                if (state.getBlock() == PowerCellSetup.powerCellBlock) {
                    blocks.add(c);
                } else if (PowerCellBlock.isAdvanced(state.getBlock())) {
                    blocks.add(c);
                    advancedBlocks++;
                } else {
                    Logging.log("Warning! Powercell network data was not up-to-date!");
                }
            }

        }

        public void add(World world, GlobalCoordinate g, boolean advanced) {
            if (!blocks.contains(g)) {
                blocks.add(g);
                costFactor = null;
                if (advanced) {
                    advancedBlocks++;
                }
                updateNetwork(world);
            }
        }

        public void remove(World world, GlobalCoordinate g, boolean advanced) {
            if (blocks.contains(g)) {
                blocks.remove(g);
                costFactor = null;
                if (advanced) {
                    advancedBlocks--;
                }
                updateNetwork(world);
            }
        }

        private double calculateBlobDistance(World world, Set<GlobalCoordinate> blob1, Set<GlobalCoordinate> blob2) {
            GlobalCoordinate c1 = blob1.iterator().next();
            GlobalCoordinate c2 = blob2.iterator().next();

            boolean dim1rftools = RFTools.instance.rftoolsDimensions && RFToolsDimensionChecker.isRFToolsDimension(world, c1.getDimension());
            boolean dim2rftools = RFTools.instance.rftoolsDimensions && RFToolsDimensionChecker.isRFToolsDimension(world, c2.getDimension());
            double rftoolsdimMult = 1.0;
            if (dim1rftools) {
                rftoolsdimMult *= PowerCellConfiguration.powerCellRFToolsDimensionAdvantage;
            }
            if (dim2rftools) {
                rftoolsdimMult *= PowerCellConfiguration.powerCellRFToolsDimensionAdvantage;
            }

            if (c1.getDimension() != c2.getDimension()) {
                return PowerCellConfiguration.powerCellDistanceCap * rftoolsdimMult;
            }
            double dist = Math.sqrt(c1.getCoordinate().distanceSq(c2.getCoordinate()));
            if (dist > PowerCellConfiguration.powerCellDistanceCap) {
                dist = PowerCellConfiguration.powerCellDistanceCap;
            } else if (dist < PowerCellConfiguration.powerCellMinDistance) {
                dist = PowerCellConfiguration.powerCellMinDistance;
            }
            return dist * rftoolsdimMult;
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
                            double dist = calculateBlobDistance(world, blob, blob2);

                            // 'part' is a number indicating how relevant this blob is for calculating
                            // the extraction cost. A big blob will have a big influence. If there is only
                            // one blob then this will be equal to 1.
                            double part = (double) blob2.size() / blocks.size();

                            // 'factor' indicates the cost of getting power out of blocks part of 'blob2'
                            // from the perspective of 'blob'.
                            double factor = 1 + (dist / PowerCellConfiguration.powerCellDistanceCap) * (PowerCellConfiguration.powerCellCostFactor - 1) * part;

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

        public int extractEnergySingleBlock(boolean advanced) {
            // Calculate the average energy with advanced blocks seen as the equivalent number of normal blocks
            int rc = energy / Math.max(1, (blocks.size() - advancedBlocks) + advancedBlocks * PowerCellConfiguration.advancedFactor);
            if (advanced) {
                rc *= PowerCellConfiguration.advancedFactor;
            }
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
            tagCompound.setInteger("advanced", advancedBlocks);
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
            this.advancedBlocks = tagCompound.getInteger("advanced");
            blocks.clear();
            NBTTagList list = tagCompound.getTagList("blocks", Constants.NBT.TAG_COMPOUND);
            for (int i = 0 ; i < list.tagCount() ; i++) {
                NBTTagCompound tag = list.getCompoundTagAt(i);
                blocks.add(new GlobalCoordinate(new BlockPos(tag.getInteger("x"), tag.getInteger("y"), tag.getInteger("z")), tag.getInteger("dim")));
            }
        }
    }
}
