package mcjty.rftools.playerprops;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;

public class PlayerExtendedProperties implements IExtendedEntityProperties {
    public static final String ID = "RFToolsPlayerProperties";

    private BuffProperties buffProperties;
    private PorterProperties porterProperties;
    private FavoriteDestinationsProperties favoriteDestinationsProperties;
    private PreferencesProperties preferencesProperties;

    public PlayerExtendedProperties() {
        buffProperties = new BuffProperties();
        porterProperties = new PorterProperties();
        favoriteDestinationsProperties = new FavoriteDestinationsProperties();
        preferencesProperties = new PreferencesProperties();
    }

    public static PlayerExtendedProperties getProperties(EntityPlayer player) {
        IExtendedEntityProperties properties = player.getExtendedProperties(ID);
        return (PlayerExtendedProperties) properties;
    }

    public void tick() {
        porterProperties.tickTeleport();
        buffProperties.tickBuffs();
        preferencesProperties.tick();
    }

    @Override
    public void saveNBTData(NBTTagCompound compound) {
        porterProperties.saveNBTData(compound);
        buffProperties.saveNBTData(compound);
        favoriteDestinationsProperties.saveNBTData(compound);
        preferencesProperties.saveNBTData(compound);
    }


    @Override
    public void loadNBTData(NBTTagCompound compound) {
        porterProperties.loadNBTData(compound);
        buffProperties.loadNBTData(compound);
        favoriteDestinationsProperties.loadNBTData(compound);
        preferencesProperties.loadNBTData(compound);
    }


    @Override
    public void init(Entity entity, World world) {
        buffProperties.setEntity(entity);
        porterProperties.setEntity(entity);
        preferencesProperties.setEntity(entity);
    }

    public BuffProperties getBuffProperties() {
        return buffProperties;
    }

    public PorterProperties getPorterProperties() {
        return porterProperties;
    }

    public FavoriteDestinationsProperties getFavoriteDestinationsProperties() {
        return favoriteDestinationsProperties;
    }

    public PreferencesProperties getPreferencesProperties() {
        return preferencesProperties;
    }
}
