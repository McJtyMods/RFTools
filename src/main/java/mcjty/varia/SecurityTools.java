package mcjty.varia;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public class SecurityTools {
    public static boolean isAdmin(EntityPlayer player) {
        return player.capabilities.isCreativeMode || MinecraftServer.getServer().getConfigurationManager().func_152596_g(player.getGameProfile());
    }
}
