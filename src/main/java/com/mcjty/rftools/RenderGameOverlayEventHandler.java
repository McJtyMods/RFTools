package com.mcjty.rftools;

import com.mcjty.gui.RenderHelper;
import com.mcjty.rftools.items.ModItems;
import net.minecraft.client.Minecraft;
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

        int x = Preferences.getBuffBarX();
        int y = Preferences.getBuffBarY();
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
                    item = ModItems.featherFallingEModuleItem;
                    break;
                case BUFF_FEATHERFALLINGPLUS:
                    item = ModItems.featherFallingPlusEModuleItem;
                    break;
                case BUFF_HASTE:
                    item = ModItems.hasteEModuleItem;
                    break;
                case BUFF_HASTEPLUS:
                    item = ModItems.hastePlusEModuleItem;
                    break;
                case BUFF_REGENERATION:
                    item = ModItems.regenerationEModuleItem;
                    break;
                case BUFF_REGENERATIONPLUS:
                    item = ModItems.regenerationPlusEModuleItem;
                    break;
                case BUFF_SATURATION:
                    item = ModItems.saturationEModuleItem;
                    break;
                case BUFF_SATURATIONPLUS:
                    item = ModItems.saturationPlusEModuleItem;
                    break;
                case BUFF_SPEED:
                    item = ModItems.speedEModuleItem;
                    break;
                case BUFF_SPEEDPLUS:
                    item = ModItems.speedPlusEModuleItem;
                    break;
                case BUFF_FLIGHT:
                    item = ModItems.flightEModuleItem;
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
