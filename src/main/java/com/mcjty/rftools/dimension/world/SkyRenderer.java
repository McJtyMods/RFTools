package com.mcjty.rftools.dimension.world;

import com.mcjty.rftools.RFTools;
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
    private static final ResourceLocation locationPlasmaSkyPng = new ResourceLocation(RFTools.MODID + ":" +"textures/sky/plasmasky.png");
//    private static final ResourceLocation locationDebugSkyPng = new ResourceLocation(RFTools.MODID + ":" +"textures/sky/debugsky.png");

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

    public static void registerPlasmaSky(GenericWorldProvider provider) {
        provider.setSkyRenderer(new IRenderHandler() {
            @Override
            public void render(float partialTicks, WorldClient world, Minecraft mc) {
                SkyRenderer.renderPlasmaSky();
            }
        });
        provider.setCloudRenderer(new IRenderHandler() {
            @Override
            public void render(float partialTicks, WorldClient world, Minecraft mc) {
            }
        });
    }

    private static class UV {
        private final double u;
        private final double v;

        private UV(double u, double v) {
            this.u = u;
            this.v = v;
        }

        public static UV uv(double u, double v) {
            return new UV(u, v);
        }
    }


    private static UV[] faceDown  = new UV[] { UV.uv(0.0D, 1.0D), UV.uv(0.0D, 0.0D), UV.uv(1.0D, 0.0D), UV.uv(1.0D, 1.0D) };
    private static UV[] faceUp    = new UV[] { UV.uv(0.0D, 1.0D), UV.uv(0.0D, 0.0D), UV.uv(1.0D, 0.0D), UV.uv(1.0D, 1.0D) };
    private static UV[] faceNorth = new UV[] { UV.uv(0.0D, 0.0D), UV.uv(0.0D, 1.0D), UV.uv(1.0D, 1.0D), UV.uv(1.0D, 0.0D) };
    private static UV[] faceSouth = new UV[] { UV.uv(1.0D, 1.0D), UV.uv(1.0D, 0.0D), UV.uv(0.0D, 0.0D), UV.uv(0.0D, 1.0D) };
    private static UV[] faceWest  = new UV[] { UV.uv(1.0D, 0.0D), UV.uv(0.0D, 0.0D), UV.uv(0.0D, 1.0D), UV.uv(1.0D, 1.0D) };
    private static UV[] faceEast  = new UV[] { UV.uv(0.0D, 1.0D), UV.uv(1.0D, 1.0D), UV.uv(1.0D, 0.0D), UV.uv(0.0D, 0.0D) };

    @SideOnly(Side.CLIENT)
    public static void renderPlasmaSky() {
        TextureManager renderEngine = Minecraft.getMinecraft().getTextureManager();

        GL11.glDisable(GL11.GL_FOG);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        RenderHelper.disableStandardItemLighting();
        GL11.glDepthMask(false);
        renderEngine.bindTexture(locationPlasmaSkyPng);
        Tessellator tessellator = Tessellator.instance;

        for (int i = 0; i < 6; ++i) {
            GL11.glPushMatrix();

            UV[] uv = faceDown;
            int col = 0xffffff;

            if (i == 0) {       // Down face
                col = 0;
            } else if (i == 1) {       // North face
                GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
                uv = faceNorth;
            } else if (i == 2) {       // South face
                GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
                uv = faceSouth;
            } else if (i == 3) {       // Up face
                GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
                uv = faceUp;
                col = 0;
            } else if (i == 4) {       // East face
                GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
                uv = faceEast;
            } else if (i == 5) {       // West face
                GL11.glRotatef(-90.0F, 0.0F, 0.0F, 1.0F);
                uv = faceWest;
            }

            tessellator.startDrawingQuads();
            tessellator.setColorOpaque_I(col);
            tessellator.addVertexWithUV(-100.0D, -100.0D, -100.0D, uv[0].u, uv[0].v);
            tessellator.addVertexWithUV(-100.0D, -100.0D, 100.0D, uv[1].u, uv[1].v);
            tessellator.addVertexWithUV(100.0D, -100.0D, 100.0D, uv[2].u, uv[2].v);
            tessellator.addVertexWithUV(100.0D, -100.0D, -100.0D, uv[3].u, uv[3].v);
            tessellator.draw();
            GL11.glPopMatrix();
        }

        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
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

        for (int i = 0; i < 6; ++i) {
            GL11.glPushMatrix();

            if (i == 1) {
                GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
            }

            if (i == 2) {
                GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
            }

            if (i == 3) {
                GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
            }

            if (i == 4) {
                GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
            }

            if (i == 5) {
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
