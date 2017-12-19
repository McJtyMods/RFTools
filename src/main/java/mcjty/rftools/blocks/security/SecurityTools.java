package mcjty.rftools.blocks.security;

import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

public class SecurityTools {

    public static void returnSecurityInfo(EntityPlayer player, int id) {
        SecurityChannels channels = SecurityChannels.getChannels(player.getEntityWorld());
        SecurityChannels.SecurityChannel channel = channels.getChannel(id);
        if (channel == null) {
            return;
        }
        RFToolsMessages.INSTANCE.sendTo(new PacketSecurityInfoReady(channel), (EntityPlayerMP) player);
    }

    public static void returnSecurityName(EntityPlayer player, int id) {
        SecurityChannels channels = SecurityChannels.getChannels(player.getEntityWorld());
        SecurityChannels.SecurityChannel channel = channels.getChannel(id);
        if (channel == null) {
            return;
        }
        RFToolsMessages.INSTANCE.sendTo(new PacketSecurityNameReady(channel), (EntityPlayerMP) player);
    }
}
