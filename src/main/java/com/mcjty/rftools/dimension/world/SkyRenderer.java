package com.mcjty.rftools.dimension.world;

import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.dimension.DimensionInformation;
import com.mcjty.rftools.dimension.description.CelestialBodyDescriptor;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.IRenderHandler;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.Random;

public class SkyRenderer {
    private static final ResourceLocation locationEndSkyPng = new ResourceLocation("textures/environment/end_sky.png");
    private static final ResourceLocation locationPlasmaSkyPng = new ResourceLocation(RFTools.MODID + ":" +"textures/sky/plasmasky.png");
//    private static final ResourceLocation locationDebugSkyPng = new ResourceLocation(RFTools.MODID + ":" +"textures/sky/debugsky.png");

    private static final ResourceLocation locationMoonPhasesPng = new ResourceLocation("textures/environment/moon_phases.png");
    private static final ResourceLocation locationSunPng = new ResourceLocation("textures/environment/sun.png");
    private static final ResourceLocation locationPlanetPng = new ResourceLocation(RFTools.MODID + ":" +"textures/sky/planet1.png");
//    private static final ResourceLocation locationCloudsPng = new ResourceLocation("textures/environment/clouds.png");

    private static boolean initialized = false;
    /** The star GL Call list */
    private static int starGLCallList;
    /** OpenGL sky list */
    private static int glSkyList;
    /** OpenGL sky list 2 */
    private static int glSkyList2;

    private static void initialize() {
        if (!initialized) {
            initialized = true;

            starGLCallList = GLAllocation.generateDisplayLists(3);
            GL11.glPushMatrix();
            GL11.glNewList(starGLCallList, GL11.GL_COMPILE);
            renderStars();
            GL11.glEndList();
            GL11.glPopMatrix();
            Tessellator tessellator = Tessellator.instance;
            glSkyList = starGLCallList + 1;
            GL11.glNewList(glSkyList, GL11.GL_COMPILE);
            byte b2 = 64;
            int i = 256 / b2 + 2;
            float f = 16.0F;
            int j;
            int k;

            for (j = -b2 * i; j <= b2 * i; j += b2) {
                for (k = -b2 * i; k <= b2 * i; k += b2) {
                    tessellator.startDrawingQuads();
                    tessellator.addVertex((j + 0), f, (k + 0));
                    tessellator.addVertex((j + b2), f, (k + 0));
                    tessellator.addVertex((j + b2), f, (k + b2));
                    tessellator.addVertex((j + 0), f, (k + b2));
                    tessellator.draw();
                }
            }

            GL11.glEndList();
            glSkyList2 = starGLCallList + 2;
            GL11.glNewList(glSkyList2, GL11.GL_COMPILE);
            f = -16.0F;
            tessellator.startDrawingQuads();

            for (j = -b2 * i; j <= b2 * i; j += b2) {
                for (k = -b2 * i; k <= b2 * i; k += b2) {
                    tessellator.addVertex((j + b2), f, (k + 0));
                    tessellator.addVertex((j + 0), f, (k + 0));
                    tessellator.addVertex((j + 0), f, (k + b2));
                    tessellator.addVertex((j + b2), f, (k + b2));
                }
            }

            tessellator.draw();
            GL11.glEndList();

        }
    }

    public static void registerNoSky(GenericWorldProvider provider) {
        provider.setSkyRenderer(new IRenderHandler() {
            @Override
            public void render(float partialTicks, WorldClient world, Minecraft mc) {
            }
        });
        provider.setCloudRenderer(new IRenderHandler() {
            @Override
            public void render(float partialTicks, WorldClient world, Minecraft mc) {
            }
        });
    }

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

    public static void registerSky(GenericWorldProvider provider, final DimensionInformation information) {
        provider.setSkyRenderer(new IRenderHandler() {
            @Override
            public void render(float partialTicks, WorldClient world, Minecraft mc) {
                SkyRenderer.renderSky(partialTicks, information);
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
    private static void renderPlasmaSky() {
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
    private static void renderEnderSky() {
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

    /**
     * Renders the sky with the partial tick time. Args: partialTickTime
     */
    @SideOnly(Side.CLIENT)
    private static void renderSky(float partialTickTime, DimensionInformation information) {
        initialize();

        EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
        WorldClient world = Minecraft.getMinecraft().theWorld;
        TextureManager renderEngine = Minecraft.getMinecraft().getTextureManager();

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        Vec3 vec3 = world.getSkyColor(player, partialTickTime);
        float skyRed = (float) vec3.xCoord;
        float skyGreen = (float) vec3.yCoord;
        float skyBlue = (float) vec3.zCoord;
        float f6;

        boolean anaglyph = Minecraft.getMinecraft().gameSettings.anaglyph;
        if (anaglyph) {
            float f4 = (skyRed * 30.0F + skyGreen * 59.0F + skyBlue * 11.0F) / 100.0F;
            float f5 = (skyRed * 30.0F + skyGreen * 70.0F) / 100.0F;
            f6 = (skyRed * 30.0F + skyBlue * 70.0F) / 100.0F;
            skyRed = f4;
            skyGreen = f5;
            skyBlue = f6;
        }

        GL11.glColor3f(skyRed, skyGreen, skyBlue);
        Tessellator tessellator = Tessellator.instance;
        GL11.glDepthMask(false);
        GL11.glEnable(GL11.GL_FOG);
        GL11.glColor3f(skyRed, skyGreen, skyBlue);
        GL11.glCallList(glSkyList);
        GL11.glDisable(GL11.GL_FOG);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        RenderHelper.disableStandardItemLighting();
        float[] sunsetColors = world.provider.calcSunriseSunsetColors(world.getCelestialAngle(partialTickTime), partialTickTime);
        float f7;
        float f8;
        float f9;
        float f10;

        if (sunsetColors != null) {
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            GL11.glPushMatrix();
            GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(MathHelper.sin(world.getCelestialAngleRadians(partialTickTime)) < 0.0F ? 180.0F : 0.0F, 0.0F, 0.0F, 1.0F);
            GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
            f6 = sunsetColors[0];
            f7 = sunsetColors[1];
            f8 = sunsetColors[2];
            float f11;

            if (anaglyph) {
                f9 = (f6 * 30.0F + f7 * 59.0F + f8 * 11.0F) / 100.0F;
                f10 = (f6 * 30.0F + f7 * 70.0F) / 100.0F;
                f11 = (f6 * 30.0F + f8 * 70.0F) / 100.0F;
                f6 = f9;
                f7 = f10;
                f8 = f11;
            }

            tessellator.startDrawing(6);
            tessellator.setColorRGBA_F(f6, f7, f8, sunsetColors[3]);
            tessellator.addVertex(0.0D, 100.0D, 0.0D);
            byte b0 = 16;
            tessellator.setColorRGBA_F(sunsetColors[0], sunsetColors[1], sunsetColors[2], 0.0F);

            for (int j = 0; j <= b0; ++j) {
                f11 = j * (float) Math.PI * 2.0F / b0;
                float f12 = MathHelper.sin(f11);
                float f13 = MathHelper.cos(f11);
                tessellator.addVertex((f12 * 120.0F), (f13 * 120.0F), (-f13 * 40.0F * sunsetColors[3]));
            }

            tessellator.draw();
            GL11.glPopMatrix();
            GL11.glShadeModel(GL11.GL_FLAT);
        }

        renderCelestialBodies(partialTickTime, information, world, renderEngine, tessellator);

        GL11.glColor3f(0.0F, 0.0F, 0.0F);
        double d0 = player.getPosition(partialTickTime).yCoord - world.getHorizon();

        if (d0 < 0.0D) {
            GL11.glPushMatrix();
            GL11.glTranslatef(0.0F, 12.0F, 0.0F);
            GL11.glCallList(glSkyList2);
            GL11.glPopMatrix();
            f8 = 1.0F;
            f9 = -((float) (d0 + 65.0D));
            f10 = -f8;
            tessellator.startDrawingQuads();
            tessellator.setColorRGBA_I(0, 255);
            tessellator.addVertex((-f8), f9, f8);
            tessellator.addVertex(f8, f9, f8);
            tessellator.addVertex(f8, f10, f8);
            tessellator.addVertex((-f8), f10, f8);
            tessellator.addVertex((-f8), f10, (-f8));
            tessellator.addVertex(f8, f10, (-f8));
            tessellator.addVertex(f8, f9, (-f8));
            tessellator.addVertex((-f8), f9, (-f8));
            tessellator.addVertex(f8, f10, (-f8));
            tessellator.addVertex(f8, f10, f8);
            tessellator.addVertex(f8, f9, f8);
            tessellator.addVertex(f8, f9, (-f8));
            tessellator.addVertex((-f8), f9, (-f8));
            tessellator.addVertex((-f8), f9, f8);
            tessellator.addVertex((-f8), f10, f8);
            tessellator.addVertex((-f8), f10, (-f8));
            tessellator.addVertex((-f8), f10, (-f8));
            tessellator.addVertex((-f8), f10, f8);
            tessellator.addVertex(f8, f10, f8);
            tessellator.addVertex(f8, f10, (-f8));
            tessellator.draw();
        }

        if (world.provider.isSkyColored()) {
            GL11.glColor3f(skyRed * 0.2F + 0.04F, skyGreen * 0.2F + 0.04F, skyBlue * 0.6F + 0.1F);
        } else {
            GL11.glColor3f(skyRed, skyGreen, skyBlue);
        }

        GL11.glPushMatrix();
        GL11.glTranslatef(0.0F, -((float) (d0 - 16.0D)), 0.0F);
        GL11.glCallList(glSkyList2);
        GL11.glPopMatrix();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDepthMask(true);
    }

    private static void renderCelestialBodies(float partialTickTime, DimensionInformation information, WorldClient world, TextureManager renderEngine, Tessellator tessellator) {
        List<CelestialBodyDescriptor> celestialBodies = information.getCelestialBodyDescriptors();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        OpenGlHelper.glBlendFunc(770, 1, 1, 0);
        GL11.glPushMatrix();

        float f6 = 1.0F - world.getRainStrength(partialTickTime);

        if (celestialBodies.isEmpty()) {
            GL11.glColor4f(1.0F, 1.0F, 1.0F, f6);
            GL11.glTranslatef(0.0F, 0.0F, 0.0F);
            GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(world.getCelestialAngle(partialTickTime) * 360.0F, 1.0F, 0.0F, 0.0F);
            float f10 = 30.0F;
            renderEngine.bindTexture(locationSunPng);
            tessellator.startDrawingQuads();
            tessellator.addVertexWithUV((-f10), 100.0D, (-f10), 0.0D, 0.0D);
            tessellator.addVertexWithUV(f10, 100.0D, (-f10), 1.0D, 0.0D);
            tessellator.addVertexWithUV(f10, 100.0D, f10, 1.0D, 1.0D);
            tessellator.addVertexWithUV((-f10), 100.0D, f10, 0.0D, 1.0D);
            tessellator.draw();
            f10 = 20.0F;
            renderEngine.bindTexture(locationMoonPhasesPng);
            int k = world.getMoonPhase();
            int l = k % 4;
            int i1 = k / 4 % 2;
            float f14 = (l + 0) / 4.0F;
            float f15 = (i1 + 0) / 2.0F;
            float f16 = (l + 1) / 4.0F;
            float f17 = (i1 + 1) / 2.0F;
            tessellator.startDrawingQuads();
            tessellator.addVertexWithUV((-f10), -100.0D, f10, f16, f17);
            tessellator.addVertexWithUV(f10, -100.0D, f10, f14, f17);
            tessellator.addVertexWithUV(f10, -100.0D, (-f10), f14, f15);
            tessellator.addVertexWithUV((-f10), -100.0D, (-f10), f16, f15);
            tessellator.draw();
        } else {
            for (CelestialBodyDescriptor body : celestialBodies) {
                switch (body.getType()) {
                    case BODY_NONE:
                        break;
                    case BODY_SUN:
                        GL11.glColor4f(1.0F, 1.0F, 1.0F, f6);
                        renderSun(partialTickTime, world, renderEngine, tessellator, body, 30.0F);
                        break;
                    case BODY_LARGESUN:
                        GL11.glColor4f(1.0F, 1.0F, 1.0F, f6);
                        renderSun(partialTickTime, world, renderEngine, tessellator, body, 80.0F);
                        break;
                    case BODY_SMALLSUN:
                        GL11.glColor4f(1.0F, 1.0F, 1.0F, f6);
                        renderSun(partialTickTime, world, renderEngine, tessellator, body, 10.0F);
                        break;
                    case BODY_REDSUN:
                        GL11.glColor4f(1.0F, 0.0F, 0.0F, f6);
                        renderSun(partialTickTime, world, renderEngine, tessellator, body, 30.0F);
                        break;
                    case BODY_MOON:
                        GL11.glColor4f(1.0F, 1.0F, 1.0F, f6);
                        renderMoon(partialTickTime, world, renderEngine, tessellator, body, 20.0F);
                        break;
                    case BODY_LARGEMOON:
                        GL11.glColor4f(1.0F, 1.0F, 1.0F, f6);
                        renderMoon(partialTickTime, world, renderEngine, tessellator, body, 60.0F);
                        break;
                    case BODY_SMALLMOON:
                        GL11.glColor4f(1.0F, 1.0F, 1.0F, f6);
                        renderMoon(partialTickTime, world, renderEngine, tessellator, body, 10.0F);
                        break;
                    case BODY_REDMOON:
                        GL11.glColor4f(1.0F, 0.0F, 0.0F, f6);
                        renderMoon(partialTickTime, world, renderEngine, tessellator, body, 20.0F);
                        break;
                    case BODY_PLANET:
                        GL11.glColor4f(1.0F, 1.0F, 1.0F, f6);
                        renderPlanet(partialTickTime, world, renderEngine, tessellator, body, 10.0F);
                        break;
                    case BODY_LARGEPLANET:
                        GL11.glColor4f(1.0F, 1.0F, 1.0F, f6);
                        renderPlanet(partialTickTime, world, renderEngine, tessellator, body, 30.0F);
                        break;
                }
            }
        }


        GL11.glDisable(GL11.GL_TEXTURE_2D);

        float f18 = world.getStarBrightness(partialTickTime) * f6;

        if (f18 > 0.0F) {
            GL11.glColor4f(f18, f18, f18, f18);
            GL11.glCallList(starGLCallList);
        }

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_FOG);
        GL11.glPopMatrix();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
    }

    private static void renderMoon(float partialTickTime, WorldClient world, TextureManager renderEngine, Tessellator tessellator, CelestialBodyDescriptor body, float size) {
        GL11.glTranslatef(0.0F, 0.0F, 0.0F);
        GL11.glRotatef(body.getyAngle(), 0.0F, 1.0F, 0.0F);
        float angle = world.provider.calculateCelestialAngle((long)(world.getWorldInfo().getWorldTime() * body.getTimeFactor() + body.getTimeOffset()), partialTickTime);
        GL11.glRotatef(angle * 360.0F, 1.0F, 0.0F, 0.0F);
        renderEngine.bindTexture(locationMoonPhasesPng);
        int k = world.getMoonPhase();
        int l = k % 4;
        int i1 = k / 4 % 2;
        float f14 = (l + 0) / 4.0F;
        float f15 = (i1 + 0) / 2.0F;
        float f16 = (l + 1) / 4.0F;
        float f17 = (i1 + 1) / 2.0F;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV((-size), -100.0D, size, f16, f17);
        tessellator.addVertexWithUV(size, -100.0D, size, f14, f17);
        tessellator.addVertexWithUV(size, -100.0D, (-size), f14, f15);
        tessellator.addVertexWithUV((-size), -100.0D, (-size), f16, f15);
        tessellator.draw();
    }

    private static void renderSun(float partialTickTime, WorldClient world, TextureManager renderEngine, Tessellator tessellator, CelestialBodyDescriptor body, float size) {
        GL11.glTranslatef(0.0F, 0.0F, 0.0F);
        GL11.glRotatef(body.getyAngle(), 0.0F, 1.0F, 0.0F);
        float angle = world.provider.calculateCelestialAngle((long)(world.getWorldInfo().getWorldTime() * body.getTimeFactor() + body.getTimeOffset()), partialTickTime);
        GL11.glRotatef(angle * 360.0F, 1.0F, 0.0F, 0.0F);
        renderEngine.bindTexture(locationSunPng);
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV((-size), 100.0D, (-size), 0.0D, 0.0D);
        tessellator.addVertexWithUV(size, 100.0D, (-size), 1.0D, 0.0D);
        tessellator.addVertexWithUV(size, 100.0D, size, 1.0D, 1.0D);
        tessellator.addVertexWithUV((-size), 100.0D, size, 0.0D, 1.0D);
        tessellator.draw();
    }

    private static void renderPlanet(float partialTickTime, WorldClient world, TextureManager renderEngine, Tessellator tessellator, CelestialBodyDescriptor body, float size) {
        GL11.glTranslatef(0.0F, 0.0F, 0.0F);
        GL11.glRotatef(body.getyAngle(), 0.0F, 1.0F, 0.0F);
        float angle = world.provider.calculateCelestialAngle((long)(world.getWorldInfo().getWorldTime() * body.getTimeFactor() + body.getTimeOffset()), partialTickTime);
        GL11.glRotatef(angle * 360.0F, 1.0F, 0.0F, 0.0F);
        renderEngine.bindTexture(locationPlanetPng);
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV((-size), 100.0D, (-size), 0.0D, 0.0D);
        tessellator.addVertexWithUV(size, 100.0D, (-size), 1.0D, 0.0D);
        tessellator.addVertexWithUV(size, 100.0D, size, 1.0D, 1.0D);
        tessellator.addVertexWithUV((-size), 100.0D, size, 0.0D, 1.0D);
        tessellator.draw();
    }

    private static void renderStars() {
        Random random = new Random(10842L);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();

        for (int i = 0; i < 1500; ++i) {
            double d0 = (random.nextFloat() * 2.0F - 1.0F);
            double d1 = (random.nextFloat() * 2.0F - 1.0F);
            double d2 = (random.nextFloat() * 2.0F - 1.0F);
            double d3 = (0.15F + random.nextFloat() * 0.1F);
            double d4 = d0 * d0 + d1 * d1 + d2 * d2;

            if (d4 < 1.0D && d4 > 0.01D) {
                d4 = 1.0D / Math.sqrt(d4);
                d0 *= d4;
                d1 *= d4;
                d2 *= d4;
                double d5 = d0 * 100.0D;
                double d6 = d1 * 100.0D;
                double d7 = d2 * 100.0D;
                double d8 = Math.atan2(d0, d2);
                double d9 = Math.sin(d8);
                double d10 = Math.cos(d8);
                double d11 = Math.atan2(Math.sqrt(d0 * d0 + d2 * d2), d1);
                double d12 = Math.sin(d11);
                double d13 = Math.cos(d11);
                double d14 = random.nextDouble() * Math.PI * 2.0D;
                double d15 = Math.sin(d14);
                double d16 = Math.cos(d14);

                for (int j = 0; j < 4; ++j) {
                    double d17 = 0.0D;
                    double d18 = ((j & 2) - 1) * d3;
                    double d19 = ((j + 1 & 2) - 1) * d3;
                    double d20 = d18 * d16 - d19 * d15;
                    double d21 = d19 * d16 + d18 * d15;
                    double d22 = d20 * d12 + d17 * d13;
                    double d23 = d17 * d12 - d20 * d13;
                    double d24 = d23 * d9 - d21 * d10;
                    double d25 = d21 * d9 + d23 * d10;
                    tessellator.addVertex(d5 + d24, d6 + d22, d7 + d25);
                }
            }
        }

        tessellator.draw();
    }


}
