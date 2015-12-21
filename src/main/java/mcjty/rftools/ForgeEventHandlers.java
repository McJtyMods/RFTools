package mcjty.rftools;

import mcjty.lib.preferences.PlayerPreferencesProperties;
import mcjty.rftools.network.RFToolsMessages;
import mcjty.rftools.playerprops.PlayerExtendedProperties;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.IExtendedEntityProperties;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class ForgeEventHandlers {

    @SubscribeEvent
    public void onPlayerTickEvent(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START && !event.player.worldObj.isRemote) {
            IExtendedEntityProperties properties = event.player.getExtendedProperties(PlayerExtendedProperties.ID);
            if (properties instanceof PlayerExtendedProperties) {
                PlayerExtendedProperties playerExtendedProperties = (PlayerExtendedProperties) properties;
                playerExtendedProperties.tick();
            }
            properties = event.player.getExtendedProperties(PlayerPreferencesProperties.ID);
            if (properties instanceof PlayerPreferencesProperties) {
                PlayerPreferencesProperties preferencesProperties = (PlayerPreferencesProperties) properties;
                preferencesProperties.tick(RFToolsMessages.INSTANCE);
            }
        }
    }

    @SubscribeEvent
    public void onEntityConstructingEvent(EntityEvent.EntityConstructing event) {
        if (event.entity instanceof EntityPlayer) {
            PlayerExtendedProperties properties = new PlayerExtendedProperties();
            event.entity.registerExtendedProperties(PlayerExtendedProperties.ID, properties);

            PlayerPreferencesProperties preferencesProperties = (PlayerPreferencesProperties) event.entity.getExtendedProperties(PlayerPreferencesProperties.ID);
            if (preferencesProperties == null) {
                preferencesProperties = new PlayerPreferencesProperties();
                event.entity.registerExtendedProperties(PlayerPreferencesProperties.ID, preferencesProperties);
            }
        }
    }


}
