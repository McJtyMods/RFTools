package com.mcjty.gui;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class RenderHelper {
    public static boolean renderObject(Minecraft mc, int x, int y, Object itm, boolean highlight) {
        RenderItem itemRender = new RenderItem();
        return renderObject(mc, itemRender, x, y, itm, highlight, 150);
    }

    public static boolean renderObject(Minecraft mc, RenderItem itemRender, int x, int y, Object itm, boolean highlight, float lvl) {
        itemRender.zLevel = lvl;

        if (itm==null) {
            return renderItemStack(mc, itemRender, null, x, y, "", highlight);
        }
        if (itm instanceof Item) {
            return renderItemStack(mc, itemRender, new ItemStack((Item) itm, 1), x, y, "", highlight);
        }
        if (itm instanceof Block) {
            return renderItemStack(mc, itemRender, new ItemStack((Block) itm, 1), x, y, "", highlight);
        }
        if (itm instanceof ItemStack) {
            return renderItemStackWithCount(mc, itemRender, (ItemStack) itm, x, y, highlight);
        }
        return renderItemStack(mc, itemRender, null,x,y,"",highlight);
    }

    public static boolean renderItemStackWithCount(Minecraft mc, RenderItem itemRender, ItemStack itm, int xo, int yo, boolean highlight) {
        if (itm.stackSize==1) {
            return renderItemStack(mc, itemRender, itm, xo, yo, "", highlight);
        } else {
            return renderItemStack(mc, itemRender, itm, xo, yo, "" + itm.stackSize, highlight);
        }
    }

    public static boolean renderItemStack(Minecraft mc, RenderItem itemRender, ItemStack itm, int x, int y, String txt, boolean highlight){
        GL11.glColor3f(1F, 1F, 1F);

        boolean isLightingEnabled = GL11.glIsEnabled(GL11.GL_LIGHTING);
        boolean isRescaleNormalEnabled = GL11.glIsEnabled(GL12.GL_RESCALE_NORMAL);

        if (highlight){
            GL11.glDisable(GL11.GL_LIGHTING);
            drawVerticalGradientRect(x, y, x+16, y+16, 0x80ffffff, 0xffffffff);
        }
        if (itm==null) {
            return false;
        }
        GL11.glPushMatrix();
        GL11.glTranslatef(0.0F, 0.0F, 32.0F);
        GL11.glColor3f(1F, 1F, 1F);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_LIGHTING);
        itemRender.renderItemAndEffectIntoGUI(mc.fontRenderer, mc.getTextureManager(), itm, x, y);
        itemRender.renderItemOverlayIntoGUI(mc.fontRenderer, mc.getTextureManager(), itm, x, y, txt);
        GL11.glPopMatrix();

        if (isLightingEnabled) {
            GL11.glEnable(GL11.GL_LIGHTING);
        } else {
            GL11.glDisable(GL11.GL_LIGHTING);
        }
        if (isRescaleNormalEnabled) {
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        } else {
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        }

        return true;
    }

    /**
     * Draws a rectangle with a vertical gradient between the specified colors.
     */
    public static void drawVerticalGradientRect(int x1, int y1, int x2, int y2, int color1, int color2) {
//        this.zLevel = 300.0F;
        float zLevel = 0.0f;

        float f = (color1 >> 24 & 255) / 255.0F;
        float f1 = (color1 >> 16 & 255) / 255.0F;
        float f2 = (color1 >> 8 & 255) / 255.0F;
        float f3 = (color1 & 255) / 255.0F;
        float f4 = (color2 >> 24 & 255) / 255.0F;
        float f5 = (color2 >> 16 & 255) / 255.0F;
        float f6 = (color2 >> 8 & 255) / 255.0F;
        float f7 = (color2 & 255) / 255.0F;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setColorRGBA_F(f1, f2, f3, f);
        tessellator.addVertex(x2, y1, zLevel);
        tessellator.addVertex(x1, y1, zLevel);
        tessellator.setColorRGBA_F(f5, f6, f7, f4);
        tessellator.addVertex(x1, y2, zLevel);
        tessellator.addVertex(x2, y2, zLevel);
        tessellator.draw();
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    /**
     * Draws a rectangle with a horizontal gradient between the specified colors.
     */
    public static void drawHorizontalGradientRect(int x1, int y1, int x2, int y2, int color1, int color2) {
//        this.zLevel = 300.0F;
        float zLevel = 0.0f;

        float f = (color1 >> 24 & 255) / 255.0F;
        float f1 = (color1 >> 16 & 255) / 255.0F;
        float f2 = (color1 >> 8 & 255) / 255.0F;
        float f3 = (color1 & 255) / 255.0F;
        float f4 = (color2 >> 24 & 255) / 255.0F;
        float f5 = (color2 >> 16 & 255) / 255.0F;
        float f6 = (color2 >> 8 & 255) / 255.0F;
        float f7 = (color2 & 255) / 255.0F;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setColorRGBA_F(f1, f2, f3, f);
        tessellator.addVertex(x1, y1, zLevel);
        tessellator.addVertex(x1, y2, zLevel);
        tessellator.setColorRGBA_F(f5, f6, f7, f4);
        tessellator.addVertex(x2, y2, zLevel);
        tessellator.addVertex(x2, y1, zLevel);
        tessellator.draw();
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }
}
