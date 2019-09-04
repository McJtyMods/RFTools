package mcjty.rftools.playerprops;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;

public class PropertiesDispatcher implements ICapabilityProvider, INBTSerializable<CompoundNBT> {

    private FavoriteDestinationsProperties favoriteDestinationsProperties = new FavoriteDestinationsProperties();
    private BuffProperties buffProperties = new BuffProperties();

    private LazyOptional<FavoriteDestinationsProperties> favoriteDestinations = LazyOptional.of(() -> favoriteDestinationsProperties);
    private LazyOptional<BuffProperties> buffs = LazyOptional.of(() -> buffProperties);

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction direction) {
        if (capability == PlayerExtendedProperties.FAVORITE_DESTINATIONS_CAPABILITY) {
            return favoriteDestinations.cast();
        }
        if (capability == PlayerExtendedProperties.BUFF_CAPABILITY) {
            return buffs.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        favoriteDestinationsProperties.saveNBTData(nbt);
        buffProperties.saveNBTData(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        favoriteDestinationsProperties.loadNBTData(nbt);
        buffProperties.loadNBTData(nbt);
    }
}
