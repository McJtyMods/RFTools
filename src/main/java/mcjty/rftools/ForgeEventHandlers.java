package mcjty.rftools;

import mcjty.rftools.playerprops.BuffProperties;
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

            BuffProperties buffProperties = PlayerExtendedProperties.getBuffProperties(event.player);
            if (buffProperties != null) {
                buffProperties.tickBuffs((EntityPlayerMP) event.player);
            }
        }
    }

    @SubscribeEvent
    public void onEntityConstructing(AttachCapabilitiesEvent.Entity event){

        if (event.getEntity() instanceof EntityPlayer) {
            if (!event.getEntity().hasCapability(PlayerExtendedProperties.PORTER_CAPABILITY, null)) {
                event.addCapability(new ResourceLocation(RFTools.MODID, "Properties"), new PropertiesDispatcher());
            }
        }
    }
}
