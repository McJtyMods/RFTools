package mcjty.rftools;

import mcjty.gui.RenderHelper;
import mcjty.rftools.blocks.environmental.EnvironmentalSetup;
import mcjty.rftools.playerprops.PlayerExtendedProperties;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.Item;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class RenderGameOverlayEventHandler {

    private static final int BUFF_ICON_SIZE = 16;
    private static RenderItem itemRender = new RenderItem();

    public static final ResourceLocation texture = new ResourceLocation("textures/atlas/items.png");
    public static List<PlayerBuff> buffs = null;

    public static void onRender(RenderGameOverlayEvent event) {
        if (event.isCancelable() || event.type != RenderGameOverlayEvent.ElementType.EXPERIENCE) {
            return;
        }

        renderBuffs();
    }

    private static void renderBuffs() {
        if (buffs == null || buffs.isEmpty()) {
            return;
        }

        EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
        PlayerExtendedProperties properties = PlayerExtendedProperties.getProperties(player);

        int x = properties.getPreferencesProperties().getBuffX();
        int y = properties.getPreferencesProperties().getBuffY();

        if (x == -1 || y == -1) {
            return;
        }

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(GL11.GL_LIGHTING);

        Minecraft.getMinecraft().renderEngine.bindTexture(texture);

        for (PlayerBuff buff : buffs) {
            Item item;
            switch (buff) {
                case BUFF_FEATHERFALLING:
                    item = EnvironmentalSetup.featherFallingEModuleItem;
                    break;
                case BUFF_FEATHERFALLINGPLUS:
                    item = EnvironmentalSetup.featherFallingPlusEModuleItem;
                    break;
                case BUFF_HASTE:
                    item = EnvironmentalSetup.hasteEModuleItem;
                    break;
                case BUFF_HASTEPLUS:
                    item = EnvironmentalSetup.hastePlusEModuleItem;
                    break;
                case BUFF_REGENERATION:
                    item = EnvironmentalSetup.regenerationEModuleItem;
                    break;
                case BUFF_REGENERATIONPLUS:
                    item = EnvironmentalSetup.regenerationPlusEModuleItem;
                    break;
                case BUFF_SATURATION:
                    item = EnvironmentalSetup.saturationEModuleItem;
                    break;
                case BUFF_SATURATIONPLUS:
                    item = EnvironmentalSetup.saturationPlusEModuleItem;
                    break;
                case BUFF_SPEED:
                    item = EnvironmentalSetup.speedEModuleItem;
                    break;
                case BUFF_SPEEDPLUS:
                    item = EnvironmentalSetup.speedPlusEModuleItem;
                    break;
                case BUFF_FLIGHT:
                    item = EnvironmentalSetup.flightEModuleItem;
                    break;
                case BUFF_PEACEFUL:
                    item = EnvironmentalSetup.peacefulEModuleItem;
                    break;
                case BUFF_WATERBREATHING:
                    item = EnvironmentalSetup.waterBreathingEModuleItem;
                    break;
                case BUFF_NIGHTVISION:
                    item = EnvironmentalSetup.nightVisionEModuleItem;
                    break;
                default:
                    item = null;
            }
            if (item != null) {
                IIcon icon = item.getIconFromDamage(0);
                RenderHelper.renderIcon(Minecraft.getMinecraft(), itemRender, icon, x, y, false);
                x += BUFF_ICON_SIZE;
            }
        }
    }
}
