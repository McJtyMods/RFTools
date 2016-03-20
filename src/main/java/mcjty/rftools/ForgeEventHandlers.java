package mcjty.rftools;

import mcjty.lib.preferences.PlayerPreferencesProperties;
import mcjty.lib.preferences.PreferencesDispatcher;
import mcjty.lib.preferences.PreferencesProperties;
import mcjty.rftools.network.RFToolsMessages;
import mcjty.rftools.playerprops.PlayerExtendedProperties;
import mcjty.rftools.playerprops.PorterProperties;
import mcjty.rftools.playerprops.PropertiesDispatcher;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class ForgeEventHandlers {

    @SubscribeEvent
    public void onPlayerTickEvent(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START && !event.player.worldObj.isRemote) {
            PorterProperties porterProperties = PlayerExtendedProperties.getPorterProperties(event.player);
            if (porterProperties != null) {
                porterProperties.tickTeleport(event.player);
            }

            PreferencesProperties preferencesProperties = PlayerPreferencesProperties.getProperties(event.player);
            preferencesProperties.tick((EntityPlayerMP) event.player, RFToolsMessages.INSTANCE);
        }
    }

    @SubscribeEvent
    public void onEntityConstructing(AttachCapabilitiesEvent.Entity event){
        // @todo move to a mcjtylib helper
        if (event.getEntity() instanceof EntityPlayer) {
            if (!event.getEntity().hasCapability(PlayerPreferencesProperties.PREFERENCES_CAPABILITY, null)) {
                event.addCapability(new ResourceLocation("McJtyLib", "Preferences"), new PreferencesDispatcher());
            }
            if (!event.getEntity().hasCapability(PlayerExtendedProperties.PORTER_CAPABILITY, null)) {
                event.addCapability(new ResourceLocation(RFTools.MODID, "Properties"), new PropertiesDispatcher());
            }

        }
    }
}
