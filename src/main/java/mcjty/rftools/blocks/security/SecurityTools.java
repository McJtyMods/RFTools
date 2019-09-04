package mcjty.rftools.blocks.security;

import mcjty.lib.typed.TypedMap;
import mcjty.rftools.ClientCommandHandler;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.network.NetworkDirection;

public class SecurityTools {

    public static void returnSecurityInfo(PlayerEntity player, int id) {
        SecurityChannels channels = SecurityChannels.get();
        SecurityChannels.SecurityChannel channel = channels.getChannel(id);
        if (channel == null) {
            return;
        }
        RFToolsMessages.INSTANCE.sendTo(new PacketSecurityInfoReady(channel), ((ServerPlayerEntity) player).connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void returnSecurityName(PlayerEntity player, int id) {
        SecurityChannels channels = SecurityChannels.get();
        SecurityChannels.SecurityChannel channel = channels.getChannel(id);
        if (channel == null) {
            return;
        }
        RFToolsMessages.sendToClient(player, ClientCommandHandler.CMD_RETURN_SECURITY_NAME,
                TypedMap.builder().put(ClientCommandHandler.PARAM_NAME, channel.getName()));
    }
}
