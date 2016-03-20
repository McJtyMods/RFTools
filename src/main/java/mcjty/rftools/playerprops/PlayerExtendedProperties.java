package mcjty.rftools.playerprops;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.capabilities.Capability;

public class PlayerExtendedProperties {

    public static Capability<FavoriteDestinationsProperties> FAVORITE_DESTINATIONS_CAPABILITY;
    public static Capability<PorterProperties> PORTER_CAPABILITY;

    public static FavoriteDestinationsProperties getFavoriteDestinations(EntityPlayer player) {
        return player.getCapability(FAVORITE_DESTINATIONS_CAPABILITY, null);
    }

    public static PorterProperties getPorterProperties(EntityPlayer player) {
        return player.getCapability(PORTER_CAPABILITY, null);
    }
}
