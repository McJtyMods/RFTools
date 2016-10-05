package mcjty.rftools.hud;

import mcjty.rftools.network.PacketGetHudLog;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.EnumFacing;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class HudRenderer {

    public static void renderHud(IHudSupport hudSupport, double x, double y, double z) {
        renderHud(hudSupport, x, y, z, 0.0f, false);
    }

    public static void renderHud(IHudSupport hudSupport, double x, double y, double z, float scale, boolean faceVert) {
        GlStateManager.pushMatrix();
        float f3 = 0.0f;

        EnumFacing orientation = hudSupport.getBlockOrientation();
        if (orientation != null) {
            switch (orientation) {
                case NORTH:
                    f3 = 180.0F;
                    break;
                case WEST:
                    f3 = 90.0F;
                    break;
                case EAST:
                    f3 = -90.0F;
                    break;
                default:
                    f3 = 0.0f;
            }
        }

        if (faceVert) {
            GlStateManager.translate((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);
        } else {
            GlStateManager.translate((float) x + 0.5F, (float) y + 1.75F, (float) z + 0.5F);
        }
        if (orientation == null) {
            GlStateManager.rotate(-Minecraft.getMinecraft().getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
            if (faceVert) {
                GlStateManager.rotate(Minecraft.getMinecraft().getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
            }
            GlStateManager.rotate(180, 0.0F, 1.0F, 0.0F);
        } else {
            GlStateManager.rotate(-f3, 0.0F, 1.0F, 0.0F);
        }
        if (faceVert) {
//            GlStateManager.translate(0.0F, -0.2500F, -0.4375F + .4);
        } else if (hudSupport.isBlockAboveAir()) {
            GlStateManager.translate(0.0F, -0.2500F, -0.4375F + .4);
        } else {
            GlStateManager.translate(0.0F, -0.2500F, -0.4375F + .9);
        }

        RenderHelper.disableStandardItemLighting();
        Minecraft.getMinecraft().entityRenderer.disableLightmap();
        GlStateManager.disableBlend();
        GlStateManager.disableLighting();

        renderText(Minecraft.getMinecraft().fontRendererObj, hudSupport, scale);
        Minecraft.getMinecraft().entityRenderer.enableLightmap();

//        RenderHelper.enableStandardItemLighting();
        GlStateManager.enableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GlStateManager.popMatrix();
    }

    private static void renderText(FontRenderer fontrenderer, IHudSupport support, float scale) {
        float f3;
        float factor = scale + 1.0f;
        int currenty = 7;

        GlStateManager.translate(-0.5F, 0.5F, 0.07F);
        f3 = 0.0075F;
        GlStateManager.scale(f3 * factor, -f3 * factor, f3);
        GlStateManager.glNormal3f(0.0F, 0.0F, 1.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        renderLog(fontrenderer, support, currenty);
    }

    private static void renderLog(FontRenderer fontrenderer, IHudSupport support, int currenty) {
        List<String> log = support.getClientLog();
        long t = System.currentTimeMillis();
        if (t - support.getLastUpdateTime() > 250) {
            RFToolsMessages.INSTANCE.sendToServer(new PacketGetHudLog(support.getPos()));
            support.setLastUpdateTime(t);
        }

        int height = 10;
        int logsize = log.size();
        int i = 0;
        for (String s : log) {
            if (i >= logsize - 11) {
                // Check if this module has enough room
                if (currenty + height <= 124) {
                    fontrenderer.drawString(fontrenderer.trimStringToWidth(s, 115), 7, currenty, 0xffffff);
                    currenty += height;
                }
            }
            i++;
        }
    }
}
