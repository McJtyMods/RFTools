package mcjty.rftools;

import mcjty.lib.McJtyLib;
import mcjty.lib.client.RenderHelper;
import mcjty.lib.preferences.PreferencesProperties;
import mcjty.rftools.blocks.environmental.EnvironmentalSetup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class RenderGameOverlayEventHandler {

    private static final int BUFF_ICON_SIZE = 16;

    public static List<PlayerBuff> buffs = null;

    public static void onRender(RenderGameOverlayEvent event) {
        if (event.isCanceled() || event.getType() != RenderGameOverlayEvent.ElementType.POTION_ICONS) {
            return;
        }

        renderBuffs();
    }

    private static void renderBuffs() {
        if (buffs == null || buffs.isEmpty()) {
            return;
        }

        EntityPlayerSP player = Minecraft.getMinecraft().player;
        PreferencesProperties preferences = McJtyLib.getPreferencesProperties(player);

        int x = preferences.getBuffX();
        int y = preferences.getBuffY();

        if (x == -1 || y == -1) {
            return;
        }

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(GL11.GL_LIGHTING);

//        Minecraft.getMinecraft().renderEngine.bindTexture(texture);

        for (PlayerBuff buff : buffs) {
            Item item = getBuffItem(buff);
            if (item != null) {
                RenderItem itemRender = Minecraft.getMinecraft().getRenderItem();
                RenderHelper.renderItemStack(Minecraft.getMinecraft(), itemRender, new ItemStack(item), x, y, "", false);
//                itemRender.renderItem(new ItemStack(item), player, ItemCameraTransforms.TransformType.FIXED, true);
                x += BUFF_ICON_SIZE;
            }
        }
    }

    private static Item getBuffItem(PlayerBuff buff) {
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
            case BUFF_GLOWING:
                item = EnvironmentalSetup.glowingEModuleItem;
                break;
            case BUFF_WATERBREATHING:
                item = EnvironmentalSetup.waterBreathingEModuleItem;
                break;
            case BUFF_NIGHTVISION:
                item = EnvironmentalSetup.nightVisionEModuleItem;
                break;
            case BUFF_BLINDNESS:
                item = EnvironmentalSetup.blindnessEModuleItem;
                break;
            case BUFF_WEAKNESS:
                item = EnvironmentalSetup.weaknessEModuleItem;
                break;
            case BUFF_POISON:
                item = EnvironmentalSetup.poisonEModuleItem;
                break;
            case BUFF_SLOWNESS:
                item = EnvironmentalSetup.slownessEModuleItem;
                break;
            case BUFF_LUCK:
                item = EnvironmentalSetup.luckEModuleItem;
                break;
            case BUFF_NOTELEPORT:
                item = EnvironmentalSetup.noTeleportEModuleItem;
                break;
            default:
                item = null;
        }
        return item;
    }
}
