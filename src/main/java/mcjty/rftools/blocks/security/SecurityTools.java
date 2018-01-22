package mcjty.rftools.blocks.security;

import mcjty.lib.network.Arguments;
import mcjty.rftools.ClientCommandHandler;
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
        RFToolsMessages.sendToClient(player, ClientCommandHandler.CMD_RETURN_SECURITY_NAME,
                Arguments.builder().value(channel.getName()));
    }
}
