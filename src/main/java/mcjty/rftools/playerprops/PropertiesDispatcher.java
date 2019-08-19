package mcjty.rftools.playerprops;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;

public class PropertiesDispatcher implements ICapabilityProvider, INBTSerializable<CompoundNBT> {

    private FavoriteDestinationsProperties favoriteDestinationsProperties = new FavoriteDestinationsProperties();
    private BuffProperties buffProperties = new BuffProperties();

    @Override
    public boolean hasCapability(Capability<?> capability, Direction facing) {
        return capability == PlayerExtendedProperties.FAVORITE_DESTINATIONS_CAPABILITY
                || capability == PlayerExtendedProperties.BUFF_CAPABILITY;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, Direction facing) {
        if (capability == PlayerExtendedProperties.FAVORITE_DESTINATIONS_CAPABILITY) {
            return (T) favoriteDestinationsProperties;
        }
        if (capability == PlayerExtendedProperties.BUFF_CAPABILITY) {
            return (T) buffProperties;
        }
        return null;
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
