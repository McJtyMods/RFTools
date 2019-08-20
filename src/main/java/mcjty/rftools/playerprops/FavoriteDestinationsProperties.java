package mcjty.rftools.playerprops;

import mcjty.lib.varia.GlobalCoordinate;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class FavoriteDestinationsProperties {

    private Set<GlobalCoordinate> favoriteDestinations = new HashSet<>();

    public FavoriteDestinationsProperties() {
    }

    public void copyFrom(FavoriteDestinationsProperties source) {
        favoriteDestinations = new HashSet<>(source.favoriteDestinations);
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
    public void saveNBTData(CompoundNBT compound) {
        writeFavoritesToNBT(compound, favoriteDestinations);
    }

    private static void writeFavoritesToNBT(CompoundNBT tagCompound, Collection<GlobalCoordinate> destinations) {
        ListNBT lst = new ListNBT();
        for (GlobalCoordinate destination : destinations) {
            CompoundNBT tc = new CompoundNBT();
            BlockPos c = destination.getCoordinate();
            tc.setInteger("x", c.getX());
            tc.setInteger("y", c.getY());
            tc.setInteger("z", c.getZ());
            tc.setInteger("dim", destination.getDimension());
            lst.appendTag(tc);
        }
        tagCompound.setTag("destinations", lst);
    }

    public void loadNBTData(CompoundNBT compound) {
        favoriteDestinations.clear();
        readCoordinatesFromNBT(compound, favoriteDestinations);
    }

    private static void readCoordinatesFromNBT(CompoundNBT tagCompound, Set<GlobalCoordinate> destinations) {
        ListNBT lst = tagCompound.getTagList("destinations", net.minecraftforge.common.util.Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < lst.tagCount() ; i++) {
            CompoundNBT tc = lst.getCompoundTagAt(i);
            BlockPos c = new BlockPos(tc.getInteger("x"), tc.getInteger("y"), tc.getInteger("z"));
            destinations.add(new GlobalCoordinate(c, tc.getInteger("dim")));
        }
    }

}
