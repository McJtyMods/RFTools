package com.mcjty.rftools.blocks.screens;

import com.mcjty.gui.RenderHelper;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.screens.modules.EnergyBarScreenModule;
import com.mcjty.rftools.blocks.screens.modules.ItemStackScreenModule;
import com.mcjty.rftools.blocks.screens.modules.ScreenModule;
import com.mcjty.rftools.blocks.screens.modules.TextScreenModule;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.List;

@SideOnly(Side.CLIENT)
public class ScreenRenderer extends TileEntitySpecialRenderer {

    private static final ResourceLocation texture = new ResourceLocation(RFTools.MODID, "textures/blocks/screenFrame.png");
    private final ModelScreen screenModel = new ModelScreen();

    private List<ScreenModule> modules;

    public ScreenRenderer() {
        modules.add(new TextScreenModule("Large capacitor:"));
        modules.add(new EnergyBarScreenModule());
        modules.add(new TextScreenModule("Dimension 'mining':"));
        modules.add(new TextScreenModule("40000000RF").color(0x00ff00));
        modules.add(new TextScreenModule(""));
        modules.add(new TextScreenModule("Inventory:"));
        modules.add(new ItemStackScreenModule());
    }

    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float f) {
        GL11.glPushMatrix();
        float f3;

        int meta = tileEntity.getBlockMetadata();
        f3 = 0.0F;

        if (meta == 2) {
            f3 = 180.0F;
        }

        if (meta == 4) {
            f3 = 90.0F;
        }

        if (meta == 5) {
            f3 = -90.0F;
        }

        GL11.glTranslatef((float) x + 0.5F, (float) y + 0.75F, (float) z + 0.5F);
        GL11.glRotatef(-f3, 0.0F, 1.0F, 0.0F);
        GL11.glTranslatef(0.0F, -0.2500F, -0.4375F);

        renderScreenBoard();

        FontRenderer fontrenderer = this.func_147498_b();



        GL11.glPushMatrix();

        GL11.glTranslatef(-0.5F, 0.5F, 0.07F);
        f3 = 0.0075F;
        GL11.glScalef(f3, -f3, f3);
        GL11.glNormal3f(0.0F, 0.0F, -1.0F);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDepthMask(false);

        int color = 0xffffff;
        String[] test = new String[] { "Large capacitor:", "" /*"Energy: 1003554 RF"*/, "Dimension 'mining':", "Energy: 40000000RF", "", "",
            "", "Inventory:" };
        for (int i = 0; i < test.length; ++i) {
            String s = test[i];

            fontrenderer.drawString(s, 7, 7 + i * 10, (i == 1 || i == 3) ? 0x00FF00 : color);
        }


        RenderHelper.drawHorizontalGradientRect(7 + 10, 7 + 11, 7+60, 7+10+8, 0xffff0000, 0xff333300);

        GL11.glPopMatrix();

        GL11.glTranslatef(-0.5F, 0.5F, 0.07F);
        GL11.glScalef(f3, -f3, -0.0001f);

        RenderItem itemRender = new RenderItem();
        short short1 = 240;
        short short2 = 240;
        net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, short1 / 1.0F, short2 / 1.0F);
        ItemStack itm = new ItemStack(Blocks.cobblestone, 32);
        itemRender.renderItemIntoGUI(fontrenderer, Minecraft.getMinecraft().getTextureManager(), itm, 20, 90);
        itemRender.renderItemOverlayIntoGUI(fontrenderer, Minecraft.getMinecraft().getTextureManager(), itm, 20, 90);

        itm = new ItemStack(Blocks.dirt, 20);
        itemRender.renderItemIntoGUI(fontrenderer, Minecraft.getMinecraft().getTextureManager(), itm, 50, 90);
        itemRender.renderItemOverlayIntoGUI(fontrenderer, Minecraft.getMinecraft().getTextureManager(), itm, 50, 90);

        itm = new ItemStack(Blocks.glowstone, 64);
        itemRender.renderItemIntoGUI(fontrenderer, Minecraft.getMinecraft().getTextureManager(), itm, 80, 90);
        itemRender.renderItemOverlayIntoGUI(fontrenderer, Minecraft.getMinecraft().getTextureManager(), itm, 80, 90);

        GL11.glDepthMask(true);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glPopMatrix();
    }

    private void renderScreenBoard() {
        this.bindTexture(texture);
        GL11.glPushMatrix();
        GL11.glScalef(1, -1, -1);
        this.screenModel.render();

        GL11.glDepthMask(false);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setBrightness(240);
        tessellator.setColorOpaque(0, 0, 0);
        tessellator.addVertex(-.46f, .46f, -0.08f);
        tessellator.addVertex(.46f, .46f, -0.08f);
        tessellator.addVertex(.46f, -.46f, -0.08f);
        tessellator.addVertex(-.46f, -.46f, -0.08f);
        tessellator.draw();

        GL11.glPopMatrix();
    }
}
