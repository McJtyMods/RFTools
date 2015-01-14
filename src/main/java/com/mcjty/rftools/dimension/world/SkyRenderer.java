package com.mcjty.rftools.dimension.world;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IRenderHandler;
import org.lwjgl.opengl.GL11;

public class SkyRenderer {
    private static final ResourceLocation locationEndSkyPng = new ResourceLocation("textures/environment/end_sky.png");

    public static void registerEnderSky(GenericWorldProvider provider) {
        provider.setSkyRenderer(new IRenderHandler() {
            @Override
            public void render(float partialTicks, WorldClient world, Minecraft mc) {
                SkyRenderer.renderEnderSky();
            }
        });
        provider.setCloudRenderer(new IRenderHandler() {
            @Override
            public void render(float partialTicks, WorldClient world, Minecraft mc) {

            }
        });
    }


    @SideOnly(Side.CLIENT)
    public static void renderEnderSky() {
        TextureManager renderEngine = Minecraft.getMinecraft().getTextureManager();

        GL11.glDisable(GL11.GL_FOG);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        RenderHelper.disableStandardItemLighting();
        GL11.glDepthMask(false);
        renderEngine.bindTexture(locationEndSkyPng);
        Tessellator tessellator = Tessellator.instance;

        for (int i = 0; i < 6; ++i)
        {
            GL11.glPushMatrix();

            if (i == 1)
            {
                GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
            }

            if (i == 2)
            {
                GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
            }

            if (i == 3)
            {
                GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
            }

            if (i == 4)
            {
                GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
            }

            if (i == 5)
            {
                GL11.glRotatef(-90.0F, 0.0F, 0.0F, 1.0F);
            }

            tessellator.startDrawingQuads();
            tessellator.setColorOpaque_I(2631720);
            tessellator.addVertexWithUV(-100.0D, -100.0D, -100.0D, 0.0D, 0.0D);
            tessellator.addVertexWithUV(-100.0D, -100.0D, 100.0D, 0.0D, 16.0D);
            tessellator.addVertexWithUV(100.0D, -100.0D, 100.0D, 16.0D, 16.0D);
            tessellator.addVertexWithUV(100.0D, -100.0D, -100.0D, 16.0D, 0.0D);
            tessellator.draw();
            GL11.glPopMatrix();
        }

        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_ALPHA_TEST);

    }
}
