package mcjty.rftools.playerprops;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;

public class PlayerExtendedProperties implements IExtendedEntityProperties {
    public static final String ID = "RFToolsPlayerProperties";

//    private BuffProperties buffProperties;
    private PorterProperties porterProperties;
    private FavoriteDestinationsProperties favoriteDestinationsProperties;

    public PlayerExtendedProperties() {
//        buffProperties = new BuffProperties();
        porterProperties = new PorterProperties();
        favoriteDestinationsProperties = new FavoriteDestinationsProperties();
    }

    public static PlayerExtendedProperties getProperties(EntityPlayer player) {
        //@todo
//        IExtendedEntityProperties properties = player.getExtendedProperties(ID);
//        return (PlayerExtendedProperties) properties;
        return null;
    }

    public void tick() {
        porterProperties.tickTeleport();
//        buffProperties.tickBuffs();
    }

    @Override
    public void saveNBTData(NBTTagCompound compound) {
        porterProperties.saveNBTData(compound);
//        buffProperties.saveNBTData(compound);
        favoriteDestinationsProperties.saveNBTData(compound);
    }


    @Override
    public void loadNBTData(NBTTagCompound compound) {
        porterProperties.loadNBTData(compound);
//        buffProperties.loadNBTData(compound);
        favoriteDestinationsProperties.loadNBTData(compound);
    }


    @Override
    public void init(Entity entity, World world) {
//        buffProperties.setEntity(entity);
        porterProperties.setEntity(entity);
    }

//    public BuffProperties getBuffProperties() {
//        return buffProperties;
//    }

    public PorterProperties getPorterProperties() {
        return porterProperties;
    }

    public FavoriteDestinationsProperties getFavoriteDestinationsProperties() {
        return favoriteDestinationsProperties;
    }
}
