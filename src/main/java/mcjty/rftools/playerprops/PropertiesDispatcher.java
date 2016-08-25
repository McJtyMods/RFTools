package mcjty.rftools.playerprops;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;

public class PropertiesDispatcher implements ICapabilityProvider, INBTSerializable<NBTTagCompound> {

    private FavoriteDestinationsProperties favoriteDestinationsProperties = new FavoriteDestinationsProperties();
    private BuffProperties buffProperties = new BuffProperties();

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == PlayerExtendedProperties.FAVORITE_DESTINATIONS_CAPABILITY
                || capability == PlayerExtendedProperties.BUFF_CAPABILITY;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == PlayerExtendedProperties.FAVORITE_DESTINATIONS_CAPABILITY) {
            return (T) favoriteDestinationsProperties;
        }
        if (capability == PlayerExtendedProperties.BUFF_CAPABILITY) {
            return (T) buffProperties;
        }
        return null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        favoriteDestinationsProperties.saveNBTData(nbt);
        buffProperties.saveNBTData(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        favoriteDestinationsProperties.loadNBTData(nbt);
        buffProperties.loadNBTData(nbt);
    }
}
