package mcjty.rftools.playerprops;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;

@SideOnly(Side.CLIENT)
public class SendPreferencesToClientHelper {

    public static void setBuffs(PacketSendPreferencesToClient buffs) {
        EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
        PlayerExtendedProperties properties = PlayerExtendedProperties.getProperties(player);
        properties.getPreferencesProperties().setBuffXY(buffs.getBuffX(), buffs.getBuffY());
        properties.getPreferencesProperties().setStyle(buffs.getStyle().getStyle());
    }
}
