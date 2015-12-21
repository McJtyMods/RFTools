package mcjty.rftools.playerprops;

import mcjty.lib.varia.Coordinate;
import mcjty.lib.varia.GlobalCoordinate;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.BlockPos;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class FavoriteDestinationsProperties {

    private Set<GlobalCoordinate> favoriteDestinations = new HashSet<GlobalCoordinate>();

    public FavoriteDestinationsProperties() {
    }

    public boolean isDestinationFavorite(GlobalCoordinate coordinate) {
        return favoriteDestinations.contains(coordinate);
    }

    public void setDestinationFavorite(GlobalCoordinate coordinate, boolean favorite) {
        if (favorite) {
            favoriteDestinations.add(coordinate);
        } else {
            favoriteDestinations.remove(coordinate);
        }
    }
    public void saveNBTData(NBTTagCompound compound) {
        writeFavoritesToNBT(compound, favoriteDestinations);
    }

    private static void writeFavoritesToNBT(NBTTagCompound tagCompound, Collection<GlobalCoordinate> destinations) {
        NBTTagList lst = new NBTTagList();
        for (GlobalCoordinate destination : destinations) {
            NBTTagCompound tc = new NBTTagCompound();
            BlockPos c = destination.getCoordinate();
            tc.setInteger("x", c.getX());
            tc.setInteger("y", c.getY());
            tc.setInteger("z", c.getZ());
            tc.setInteger("dim", destination.getDimension());
            lst.appendTag(tc);
        }
        tagCompound.setTag("destinations", lst);
    }

    public void loadNBTData(NBTTagCompound compound) {
        favoriteDestinations.clear();
        readCoordinatesFromNBT(compound, favoriteDestinations);
    }

    private static void readCoordinatesFromNBT(NBTTagCompound tagCompound, Set<GlobalCoordinate> destinations) {
        NBTTagList lst = tagCompound.getTagList("destinations", net.minecraftforge.common.util.Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < lst.tagCount() ; i++) {
            NBTTagCompound tc = lst.getCompoundTagAt(i);
            BlockPos c = new BlockPos(tc.getInteger("x"), tc.getInteger("y"), tc.getInteger("z"));
            destinations.add(new GlobalCoordinate(c, tc.getInteger("dim")));
        }
    }

}
