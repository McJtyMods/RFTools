package mcjty.rftools.blocks.blockprotector;

import mcjty.varia.Coordinate;
import mcjty.varia.GlobalCoordinate;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import java.util.HashMap;
import java.util.Map;

public class BlockProtectors extends WorldSavedData {
    public static final String PROTECTORS_NAME = "RFToolsBlockProtectors";
    private static BlockProtectors instance = null;

    private final Map<Integer,GlobalCoordinate> protectorById = new HashMap<Integer, GlobalCoordinate>();
    private final Map<GlobalCoordinate,Integer> protectorIdByCoordinate = new HashMap<GlobalCoordinate, Integer>();
    private int lastId = 0;

    public BlockProtectors(String identifier) {
        super(identifier);
    }

    public void save(World world) {
        world.mapStorage.setData(PROTECTORS_NAME, this);
        markDirty();
    }

    public static void clearInstance() {
        if (instance != null) {
            instance.protectorById.clear();
            instance.protectorIdByCoordinate.clear();
            instance = null;
        }
    }

    public static BlockProtectors getProtectors(World world) {
        if (world.isRemote) {
            return null;
        }
        if (instance != null) {
            return instance;
        }
        instance = (BlockProtectors) world.mapStorage.loadData(BlockProtectors.class, PROTECTORS_NAME);
        if (instance == null) {
            instance = new BlockProtectors(PROTECTORS_NAME);
        }
        return instance;
    }

    // Set an old id to a new position (after moving a receiver).
    public void assignId(GlobalCoordinate key, int id) {
        protectorById.put(id, key);
        protectorIdByCoordinate.put(key, id);
    }

    public int getNewId(GlobalCoordinate key) {
        if (protectorIdByCoordinate.containsKey(key)) {
            return protectorIdByCoordinate.get(key);
        }
        lastId++;
        protectorById.put(lastId, key);
        protectorIdByCoordinate.put(key, lastId);
        return lastId;
    }

    // Get the id from a coordinate.
    public Integer getIdForCoordinate(GlobalCoordinate key) {
        return protectorIdByCoordinate.get(key);
    }

    public GlobalCoordinate getCoordinateForId(int id) {
        return protectorById.get(id);
    }

    public void removeDestination(Coordinate coordinate, int dimension) {
        GlobalCoordinate key = new GlobalCoordinate(coordinate, dimension);
        Integer id = protectorIdByCoordinate.get(key);
        if (id != null) {
            protectorById.remove(id);
            protectorIdByCoordinate.remove(key);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        protectorById.clear();
        protectorIdByCoordinate.clear();
        lastId = tagCompound.getInteger("lastId");
        readDestinationsFromNBT(tagCompound);
    }

    private void readDestinationsFromNBT(NBTTagCompound tagCompound) {
        NBTTagList lst = tagCompound.getTagList("blocks", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < lst.tagCount() ; i++) {
            NBTTagCompound tc = lst.getCompoundTagAt(i);
            Coordinate c = new Coordinate(tc.getInteger("x"), tc.getInteger("y"), tc.getInteger("z"));
            int dim = tc.getInteger("dim");

            GlobalCoordinate gc = new GlobalCoordinate(c, dim);
            int id = tc.getInteger("id");
            protectorById.put(id, gc);
            protectorIdByCoordinate.put(gc, id);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        NBTTagList lst = new NBTTagList();
        for (GlobalCoordinate destination : protectorIdByCoordinate.keySet()) {
            NBTTagCompound tc = new NBTTagCompound();
            Coordinate c = destination.getCoordinate();
            tc.setInteger("x", c.getX());
            tc.setInteger("y", c.getY());
            tc.setInteger("z", c.getZ());
            tc.setInteger("dim", destination.getDimension());
            Integer id = protectorIdByCoordinate.get(new GlobalCoordinate(c, destination.getDimension()));
            tc.setInteger("id", id);
            lst.appendTag(tc);
        }
        tagCompound.setTag("blocks", lst);
        tagCompound.setInteger("lastId", lastId);
    }

}
