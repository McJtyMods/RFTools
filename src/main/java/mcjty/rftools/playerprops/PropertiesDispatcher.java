package mcjty.rftools.playerprops;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;

public class PropertiesDispatcher implements ICapabilityProvider, INBTSerializable<NBTTagCompound> {

    private FavoriteDestinationsProperties favoriteDestinationsProperties = new FavoriteDestinationsProperties();
    private PorterProperties porterProperties = new PorterProperties();

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == PlayerExtendedProperties.PORTER_CAPABILITY || capability == PlayerExtendedProperties.FAVORITE_DESTINATIONS_CAPABILITY;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == PlayerExtendedProperties.PORTER_CAPABILITY) {
            return (T) porterProperties;
        }
        if (capability == PlayerExtendedProperties.FAVORITE_DESTINATIONS_CAPABILITY) {
            return (T) favoriteDestinationsProperties;
        }
        return null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        favoriteDestinationsProperties.saveNBTData(nbt);
        porterProperties.saveNBTData(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        favoriteDestinationsProperties.loadNBTData(nbt);
        porterProperties.loadNBTData(nbt);
    }
}
