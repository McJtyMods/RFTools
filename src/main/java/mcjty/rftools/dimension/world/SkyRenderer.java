package mcjty.rftools.dimension.world;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcjty.rftools.RFTools;
import mcjty.rftools.dimension.DimensionInformation;
import mcjty.rftools.dimension.description.CelestialBodyDescriptor;
import mcjty.rftools.dimension.world.types.SkyType;
import mcjty.rftools.items.dimlets.types.Patreons;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.*;
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
    private static final ResourceLocation locationStars1 = new ResourceLocation(RFTools.MODID + ":" +"textures/sky/stars1.png");
    private static final ResourceLocation locationStars1a = new ResourceLocation(RFTools.MODID + ":" +"textures/sky/stars1a.png");
    private static final ResourceLocation locationStars2 = new ResourceLocation(RFTools.MODID + ":" +"textures/sky/stars2.png");
    private static final ResourceLocation locationStars3 = new ResourceLocation(RFTools.MODID + ":" +"textures/sky/stars3.png");
    private static final ResourceLocation locationStars3a = new ResourceLocation(RFTools.MODID + ":" +"textures/sky/stars3a.png");
//    private static final ResourceLocation locationDebugSkyPng = new ResourceLocation(RFTools.MODID + ":" +"textures/sky/debugsky.png");

    private static final ResourceLocation locationMoonPhasesPng = new ResourceLocation("textures/environment/moon_phases.png");
    private static final ResourceLocation locationSunPng = new ResourceLocation("textures/environment/sun.png");
    private static final ResourceLocation locationSickSunPng = new ResourceLocation(RFTools.MODID + ":" +"textures/sky/sicksun.png");
    private static final ResourceLocation locationSickMoonPng = new ResourceLocation(RFTools.MODID + ":" +"textures/sky/sickmoon.png");
    private static final ResourceLocation locationRabbitSunPng = new ResourceLocation(RFTools.MODID + ":" +"textures/sky/rabbitsun.png");
    private static final ResourceLocation locationRabbitMoonPng = new ResourceLocation(RFTools.MODID + ":" +"textures/sky/rabbitmoon.png");
    private static final ResourceLocation locationPlanetPng = new ResourceLocation(RFTools.MODID + ":" +"textures/sky/planet1.png");
    private static final ResourceLocation locationWolfMoonPng = new ResourceLocation(RFTools.MODID + ":" +"textures/sky/wolfred.png");

    private static final ResourceLocation locationCloudsPng = new ResourceLocation("textures/environment/clouds.png");

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

    public static void registerCloudRenderer(final GenericWorldProvider provider, final DimensionInformation information) {
        provider.setCloudRenderer(new IRenderHandler() {
            @Override
            public void render(float partialTicks, WorldClient world, Minecraft mc) {
                renderClouds(provider, information, partialTicks);
            }
        });
    }

    private static final int SKYTYPE_DARKTOP = 0;
    private static final int SKYTYPE_ALLHORIZONTAL = 1;
    private static final int SKYTYPE_ALL = 2;
    private static final int SKYTYPE_ALTERNATING = 3;

    public static void registerSkybox(GenericWorldProvider provider, final SkyType skyType) {
        provider.setSkyRenderer(new IRenderHandler() {
            @Override
            public void render(float partialTicks, WorldClient world, Minecraft mc) {
                ResourceLocation sky;
                ResourceLocation sky2 = null;
                int type = SKYTYPE_DARKTOP;
                switch (skyType) {
                    case SKY_INFERNO:
                        sky = locationPlasmaSkyPng;
                        type = SKYTYPE_DARKTOP;
                        break;
                    case SKY_STARS1:
                        sky = locationStars1;
                        sky2 = locationStars1a;
                        type = SKYTYPE_ALTERNATING;
                        break;
                    case SKY_STARS2:
                        sky = locationStars2;
                        type = SKYTYPE_ALL;
                        break;
                    case SKY_STARS3:
                        sky = locationStars3;
                        sky2 = locationStars3a;
                        type = SKYTYPE_ALLHORIZONTAL;
                        break;
                    default:
                        return;
                }
                SkyRenderer.renderSkyTexture(sky, sky2, type);
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
    private static void renderSkyTexture(ResourceLocation sky, ResourceLocation sky2, int type) {
        TextureManager renderEngine = Minecraft.getMinecraft().getTextureManager();

        GL11.glDisable(GL11.GL_FOG);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        RenderHelper.disableStandardItemLighting();
        GL11.glDepthMask(false);
        Tessellator tessellator = Tessellator.instance;

        for (int i = 0; i < 6; ++i) {
            GL11.glPushMatrix();

            UV[] uv = faceDown;
            int col = 0xffffff;

            if (i == 0) {       // Down face
                uv = faceDown;
                switch (type) {
                    case SKYTYPE_ALL:
                        renderEngine.bindTexture(sky);
                        break;
                    case SKYTYPE_ALLHORIZONTAL:
                    case SKYTYPE_ALTERNATING:
                        renderEngine.bindTexture(sky2);
                        break;
                    default:
                        col = 0;
                        break;
                }
            } else if (i == 1) {       // North face
                renderEngine.bindTexture(sky);
                GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
                uv = faceNorth;
            } else if (i == 2) {       // South face
                renderEngine.bindTexture(sky);
                GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
                uv = faceSouth;
            } else if (i == 3) {       // Up face
                GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
                uv = faceUp;
                switch (type) {
                    case SKYTYPE_ALL:
                        renderEngine.bindTexture(sky);
                        break;
                    case SKYTYPE_ALLHORIZONTAL:
                    case SKYTYPE_ALTERNATING:
                        renderEngine.bindTexture(sky2);
                        break;
                    default:
                        col = 0;
                        break;
                }
            } else if (i == 4) {       // East face
                if (type == SKYTYPE_ALTERNATING && sky2 != null) {
                    renderEngine.bindTexture(sky2);
                } else {
                    renderEngine.bindTexture(sky);
                }
                GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
                uv = faceEast;
            } else if (i == 5) {       // West face
                if (type == SKYTYPE_ALTERNATING && sky2 != null) {
                    renderEngine.bindTexture(sky2);
                } else {
                    renderEngine.bindTexture(sky);
                }
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
        ResourceLocation sun = getSun(information);
        ResourceLocation moon = getMoon(information);

        if (celestialBodies.isEmpty()) {
            GL11.glColor4f(1.0F, 1.0F, 1.0F, f6);
            GL11.glTranslatef(0.0F, 0.0F, 0.0F);
            GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(world.getCelestialAngle(partialTickTime) * 360.0F, 1.0F, 0.0F, 0.0F);
            float f10 = 30.0F;
            renderEngine.bindTexture(sun);
            tessellator.startDrawingQuads();
            tessellator.addVertexWithUV((-f10), 100.0D, (-f10), 0.0D, 0.0D);
            tessellator.addVertexWithUV(f10, 100.0D, (-f10), 1.0D, 0.0D);
            tessellator.addVertexWithUV(f10, 100.0D, f10, 1.0D, 1.0D);
            tessellator.addVertexWithUV((-f10), 100.0D, f10, 0.0D, 1.0D);
            tessellator.draw();
            f10 = 20.0F;
            float f14, f15, f16, f17;
            renderEngine.bindTexture(moon);
            if (!moon.equals(locationMoonPhasesPng)) {
                f14 = 0.0f;
                f15 = 0.0f;
                f16 = 1.0f;
                f17 = 1.0f;
            } else {
                int k = world.getMoonPhase();
                int l = k % 4;
                int i1 = k / 4 % 2;
                f14 = (l + 0) / 4.0F;
                f15 = (i1 + 0) / 2.0F;
                f16 = (l + 1) / 4.0F;
                f17 = (i1 + 1) / 2.0F;
            }
            tessellator.startDrawingQuads();
            tessellator.addVertexWithUV((-f10), -100.0D, f10, f16, f17);
            tessellator.addVertexWithUV(f10, -100.0D, f10, f14, f17);
            tessellator.addVertexWithUV(f10, -100.0D, (-f10), f14, f15);
            tessellator.addVertexWithUV((-f10), -100.0D, (-f10), f16, f15);
            tessellator.draw();
        } else {
            Random random = new Random(world.getSeed());
            for (CelestialBodyDescriptor body : celestialBodies) {
                float offset = 0.0f;
                float factor = 1.0f;
                float yangle = -90.0f;
                if (!body.isMain()) {
                    offset = random.nextFloat() * 200.0f;
                    factor = random.nextFloat() * 3.0f;
                    yangle = random.nextFloat() * 180.0f;
                }
                switch (body.getType()) {
                    case BODY_NONE:
                        break;
                    case BODY_SUN:
                        GL11.glColor4f(1.0F, 1.0F, 1.0F, f6);
                        renderSun(partialTickTime, world, renderEngine, tessellator, offset, factor, yangle, 30.0F, sun);
                        break;
                    case BODY_LARGESUN:
                        GL11.glColor4f(1.0F, 1.0F, 1.0F, f6);
                        renderSun(partialTickTime, world, renderEngine, tessellator, offset, factor, yangle, 80.0F, sun);
                        break;
                    case BODY_SMALLSUN:
                        GL11.glColor4f(1.0F, 1.0F, 1.0F, f6);
                        renderSun(partialTickTime, world, renderEngine, tessellator, offset, factor, yangle, 10.0F, sun);
                        break;
                    case BODY_REDSUN:
                        GL11.glColor4f(1.0F, 0.0F, 0.0F, f6);
                        renderSun(partialTickTime, world, renderEngine, tessellator, offset, factor, yangle, 30.0F, sun);
                        break;
                    case BODY_MOON:
                        GL11.glColor4f(1.0F, 1.0F, 1.0F, f6);
                        renderMoon(partialTickTime, world, renderEngine, tessellator, offset, factor, yangle, 20.0F, moon);
                        break;
                    case BODY_LARGEMOON:
                        GL11.glColor4f(1.0F, 1.0F, 1.0F, f6);
                        renderMoon(partialTickTime, world, renderEngine, tessellator, offset, factor, yangle, 60.0F, moon);
                        break;
                    case BODY_SMALLMOON:
                        GL11.glColor4f(1.0F, 1.0F, 1.0F, f6);
                        renderMoon(partialTickTime, world, renderEngine, tessellator, offset, factor, yangle, 10.0F, moon);
                        break;
                    case BODY_REDMOON:
                        GL11.glColor4f(1.0F, 0.0F, 0.0F, f6);
                        renderMoon(partialTickTime, world, renderEngine, tessellator, offset, factor, yangle, 20.0F, moon);
                        break;
                    case BODY_PLANET:
                        GL11.glColor4f(1.0F, 1.0F, 1.0F, f6);
                        renderPlanet(partialTickTime, world, renderEngine, tessellator, offset, factor, yangle, 10.0F);
                        break;
                    case BODY_LARGEPLANET:
                        GL11.glColor4f(1.0F, 1.0F, 1.0F, f6);
                        renderPlanet(partialTickTime, world, renderEngine, tessellator, offset, factor, yangle, 30.0F);
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

    private static ResourceLocation getSun(DimensionInformation information) {
        ResourceLocation sun;
        if (information.isPatreonBitSet(Patreons.PATREON_SICKSUN)) {
            sun = locationSickSunPng;
        } else if (information.isPatreonBitSet(Patreons.PATREON_RABBITSUN)) {
            sun = locationRabbitSunPng;
        } else {
            sun = locationSunPng;
        }
        return sun;
    }

    private static ResourceLocation getMoon(DimensionInformation information) {
        ResourceLocation moon;
        if (information.isPatreonBitSet(Patreons.PATREON_SICKMOON)) {
            moon = locationSickMoonPng;
        } else if (information.isPatreonBitSet(Patreons.PATREON_RABBITMOON)) {
            moon = locationRabbitMoonPng;
        } else if (information.isPatreonBitSet(Patreons.PATREON_TOMWOLF)) {
            moon = locationWolfMoonPng;
        } else {
            moon = locationMoonPhasesPng;
        }
        return moon;
    }

    private static void renderMoon(float partialTickTime, WorldClient world, TextureManager renderEngine, Tessellator tessellator, float offset, float factor, float yangle, float size, ResourceLocation moon) {
        GL11.glTranslatef(0.0F, 0.0F, 0.0F);
        GL11.glRotatef(yangle, 0.0F, 1.0F, 0.0F);
        float angle = world.provider.calculateCelestialAngle(world.getWorldInfo().getWorldTime(), partialTickTime);
        angle = angle * factor + offset;
        GL11.glRotatef(angle * 360.0F, 1.0F, 0.0F, 0.0F);

        float f14, f15, f16, f17;
        renderEngine.bindTexture(moon);
        if (!moon.equals(locationMoonPhasesPng)) {
            f14 = 0.0f;
            f15 = 0.0f;
            f16 = 1.0f;
            f17 = 1.0f;
        } else {
            int k = world.getMoonPhase();
            int l = k % 4;
            int i1 = k / 4 % 2;
            f14 = (l + 0) / 4.0F;
            f15 = (i1 + 0) / 2.0F;
            f16 = (l + 1) / 4.0F;
            f17 = (i1 + 1) / 2.0F;
        }
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV((-size), -100.0D, size, f16, f17);
        tessellator.addVertexWithUV(size, -100.0D, size, f14, f17);
        tessellator.addVertexWithUV(size, -100.0D, (-size), f14, f15);
        tessellator.addVertexWithUV((-size), -100.0D, (-size), f16, f15);
        tessellator.draw();
    }

    private static void renderSun(float partialTickTime, WorldClient world, TextureManager renderEngine, Tessellator tessellator, float offset, float factor, float yangle, float size, ResourceLocation sun) {
        GL11.glTranslatef(0.0F, 0.0F, 0.0F);
        GL11.glRotatef(yangle, 0.0F, 1.0F, 0.0F);
        float angle = world.provider.calculateCelestialAngle(world.getWorldInfo().getWorldTime(), partialTickTime);
        angle = angle * factor + offset;
        GL11.glRotatef(angle * 360.0F, 1.0F, 0.0F, 0.0F);
        renderEngine.bindTexture(sun);
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV((-size), 100.0D, (-size), 0.0D, 0.0D);
        tessellator.addVertexWithUV(size, 100.0D, (-size), 1.0D, 0.0D);
        tessellator.addVertexWithUV(size, 100.0D, size, 1.0D, 1.0D);
        tessellator.addVertexWithUV((-size), 100.0D, size, 0.0D, 1.0D);
        tessellator.draw();
    }

    private static void renderPlanet(float partialTickTime, WorldClient world, TextureManager renderEngine, Tessellator tessellator, float offset, float factor, float yangle, float size) {
        GL11.glTranslatef(0.0F, 0.0F, 0.0F);
        GL11.glRotatef(yangle, 0.0F, 1.0F, 0.0F);
        float angle = world.provider.calculateCelestialAngle(world.getWorldInfo().getWorldTime(), partialTickTime);
        angle = angle * factor + offset;
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

    @SideOnly(Side.CLIENT)
    public static void renderClouds(GenericWorldProvider provider, DimensionInformation information, float partialTicks) {
        GL11.glDisable(GL11.GL_CULL_FACE);
        Minecraft mc = Minecraft.getMinecraft();
        TextureManager renderEngine = mc.getTextureManager();
        float f1 = (float) (mc.renderViewEntity.lastTickPosY + (mc.renderViewEntity.posY - mc.renderViewEntity.lastTickPosY) * partialTicks);
        Tessellator tessellator = Tessellator.instance;
        float f2 = 12.0F;
        float f3 = 4.0F;
//        double d0 = (double)((float)RenderGlobal.cloudTickCounter + partialTicks);
        double d0 = (0 + partialTicks);
        double d1 = (mc.renderViewEntity.prevPosX + (mc.renderViewEntity.posX - mc.renderViewEntity.prevPosX) * partialTicks + d0 * 0.029999999329447746D) / f2;
        double d2 = (mc.renderViewEntity.prevPosZ + (mc.renderViewEntity.posZ - mc.renderViewEntity.prevPosZ) * partialTicks) / f2 + 0.33000001311302185D;
        float f4 = provider.getCloudHeight() - f1 + 0.33F;
        int i = MathHelper.floor_double(d1 / 2048.0D);
        int j = MathHelper.floor_double(d2 / 2048.0D);
        d1 -= (i * 2048);
        d2 -= (j * 2048);
        renderEngine.bindTexture(locationCloudsPng);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        Vec3 vec3 = provider.worldObj.getCloudColour(partialTicks);
        float f5 = (float) vec3.xCoord;
        float f6 = (float) vec3.yCoord;
        float f7 = (float) vec3.zCoord;
        float f8;
        float f9;
        float f10;

        if (mc.gameSettings.anaglyph) {
            f8 = (f5 * 30.0F + f6 * 59.0F + f7 * 11.0F) / 100.0F;
            f9 = (f5 * 30.0F + f6 * 70.0F) / 100.0F;
            f10 = (f5 * 30.0F + f7 * 70.0F) / 100.0F;
            f5 = f8;
            f6 = f9;
            f7 = f10;
        }

        f10 = 0.00390625F;
        f8 = MathHelper.floor_double(d1) * f10;
        f9 = MathHelper.floor_double(d2) * f10;
        float f11 = (float) (d1 - MathHelper.floor_double(d1));
        float f12 = (float) (d2 - MathHelper.floor_double(d2));
        byte b0 = 8;
        byte b1 = 4;
        float f13 = 9.765625E-4F;
        GL11.glScalef(f2, 1.0F, f2);

        Float cr = information.getSkyDescriptor().getCloudColorFactorR();
        Float cg = information.getSkyDescriptor().getCloudColorFactorG();
        Float cb = information.getSkyDescriptor().getCloudColorFactorB();

        for (int k = 0; k < 2; ++k) {
            if (k == 0) {
                GL11.glColorMask(false, false, false, false);
            } else if (mc.gameSettings.anaglyph) {
                if (EntityRenderer.anaglyphField == 0) {
                    GL11.glColorMask(false, true, true, true);
                } else {
                    GL11.glColorMask(true, false, false, true);
                }
            } else {
                GL11.glColorMask(true, true, true, true);
            }

            for (int l = -b1 + 1; l <= b1; ++l) {
                for (int i1 = -b1 + 1; i1 <= b1; ++i1) {
                    tessellator.startDrawingQuads();
                    float f14 = (l * b0);
                    float f15 = (i1 * b0);
                    float f16 = f14 - f11;
                    float f17 = f15 - f12;

                    if (f4 > -f3 - 1.0F) {
                        tessellator.setColorRGBA_F(f5 * 0.7F * cr, f6 * 0.7F * cg, f7 * 0.7F * cb, 0.8F);
                        tessellator.setNormal(0.0F, -1.0F, 0.0F);
                        tessellator.addVertexWithUV((f16 + 0.0F), (f4 + 0.0F), (f17 + b0), ((f14 + 0.0F) * f10 + f8), ((f15 + b0) * f10 + f9));
                        tessellator.addVertexWithUV((f16 + b0), (f4 + 0.0F), (f17 + b0), ((f14 + b0) * f10 + f8), ((f15 + b0) * f10 + f9));
                        tessellator.addVertexWithUV((f16 + b0), (f4 + 0.0F), (f17 + 0.0F), ((f14 + b0) * f10 + f8), ((f15 + 0.0F) * f10 + f9));
                        tessellator.addVertexWithUV((f16 + 0.0F), (f4 + 0.0F), (f17 + 0.0F), ((f14 + 0.0F) * f10 + f8), ((f15 + 0.0F) * f10 + f9));
                    }

                    if (f4 <= f3 + 1.0F) {
                        tessellator.setColorRGBA_F(f5 * cr, f6 * cg, f7 * cb, 0.8F);
                        tessellator.setNormal(0.0F, 1.0F, 0.0F);
                        tessellator.addVertexWithUV((f16 + 0.0F), (f4 + f3 - f13), (f17 + b0), ((f14 + 0.0F) * f10 + f8), ((f15 + b0) * f10 + f9));
                        tessellator.addVertexWithUV((f16 + b0), (f4 + f3 - f13), (f17 + b0), ((f14 + b0) * f10 + f8), ((f15 + b0) * f10 + f9));
                        tessellator.addVertexWithUV((f16 + b0), (f4 + f3 - f13), (f17 + 0.0F), ((f14 + b0) * f10 + f8), ((f15 + 0.0F) * f10 + f9));
                        tessellator.addVertexWithUV((f16 + 0.0F), (f4 + f3 - f13), (f17 + 0.0F), ((f14 + 0.0F) * f10 + f8), ((f15 + 0.0F) * f10 + f9));
                    }

                    tessellator.setColorRGBA_F(f5 * 0.9F * cr, f6 * 0.9F * cg, f7 * 0.9F * cb, 0.8F);
                    int j1;

                    if (l > -1) {
                        tessellator.setNormal(-1.0F, 0.0F, 0.0F);

                        for (j1 = 0; j1 < b0; ++j1) {
                            tessellator.addVertexWithUV((f16 + j1 + 0.0F), (f4 + 0.0F), (f17 + b0), ((f14 + j1 + 0.5F) * f10 + f8), ((f15 + b0) * f10 + f9));
                            tessellator.addVertexWithUV((f16 + j1 + 0.0F), (f4 + f3), (f17 + b0), ((f14 + j1 + 0.5F) * f10 + f8), ((f15 + b0) * f10 + f9));
                            tessellator.addVertexWithUV((f16 + j1 + 0.0F), (f4 + f3), (f17 + 0.0F), ((f14 + j1 + 0.5F) * f10 + f8), ((f15 + 0.0F) * f10 + f9));
                            tessellator.addVertexWithUV((f16 + j1 + 0.0F), (f4 + 0.0F), (f17 + 0.0F), ((f14 + j1 + 0.5F) * f10 + f8), ((f15 + 0.0F) * f10 + f9));
                        }
                    }

                    if (l <= 1) {
                        tessellator.setNormal(1.0F, 0.0F, 0.0F);

                        for (j1 = 0; j1 < b0; ++j1) {
                            tessellator.addVertexWithUV((f16 + j1 + 1.0F - f13), (f4 + 0.0F), (f17 + b0), ((f14 + j1 + 0.5F) * f10 + f8), ((f15 + b0) * f10 + f9));
                            tessellator.addVertexWithUV((f16 + j1 + 1.0F - f13), (f4 + f3), (f17 + b0), ((f14 + j1 + 0.5F) * f10 + f8), ((f15 + b0) * f10 + f9));
                            tessellator.addVertexWithUV((f16 + j1 + 1.0F - f13), (f4 + f3), (f17 + 0.0F), ((f14 + j1 + 0.5F) * f10 + f8), ((f15 + 0.0F) * f10 + f9));
                            tessellator.addVertexWithUV((f16 + j1 + 1.0F - f13), (f4 + 0.0F), (f17 + 0.0F), ((f14 + j1 + 0.5F) * f10 + f8), ((f15 + 0.0F) * f10 + f9));
                        }
                    }

                    tessellator.setColorRGBA_F(f5 * 0.8F * cr, f6 * 0.8F * cg, f7 * 0.8F * cb, 0.8F);

                    if (i1 > -1) {
                        tessellator.setNormal(0.0F, 0.0F, -1.0F);

                        for (j1 = 0; j1 < b0; ++j1) {
                            tessellator.addVertexWithUV((f16 + 0.0F), (f4 + f3), (f17 + j1 + 0.0F), ((f14 + 0.0F) * f10 + f8), ((f15 + j1 + 0.5F) * f10 + f9));
                            tessellator.addVertexWithUV((f16 + b0), (f4 + f3), (f17 + j1 + 0.0F), ((f14 + b0) * f10 + f8), ((f15 + j1 + 0.5F) * f10 + f9));
                            tessellator.addVertexWithUV((f16 + b0), (f4 + 0.0F), (f17 + j1 + 0.0F), ((f14 + b0) * f10 + f8), ((f15 + j1 + 0.5F) * f10 + f9));
                            tessellator.addVertexWithUV((f16 + 0.0F), (f4 + 0.0F), (f17 + j1 + 0.0F), ((f14 + 0.0F) * f10 + f8), ((f15 + j1 + 0.5F) * f10 + f9));
                        }
                    }

                    if (i1 <= 1) {
                        tessellator.setNormal(0.0F, 0.0F, 1.0F);

                        for (j1 = 0; j1 < b0; ++j1) {
                            tessellator.addVertexWithUV((f16 + 0.0F), (f4 + f3), (f17 + j1 + 1.0F - f13), ((f14 + 0.0F) * f10 + f8), ((f15 + j1 + 0.5F) * f10 + f9));
                            tessellator.addVertexWithUV((f16 + b0), (f4 + f3), (f17 + j1 + 1.0F - f13), ((f14 + b0) * f10 + f8), ((f15 + j1 + 0.5F) * f10 + f9));
                            tessellator.addVertexWithUV((f16 + b0), (f4 + 0.0F), (f17 + j1 + 1.0F - f13), ((f14 + b0) * f10 + f8), ((f15 + j1 + 0.5F) * f10 + f9));
                            tessellator.addVertexWithUV((f16 + 0.0F), (f4 + 0.0F), (f17 + j1 + 1.0F - f13), ((f14 + 0.0F) * f10 + f8), ((f15 + j1 + 0.5F) * f10 + f9));
                        }
                    }

                    tessellator.draw();
                }
            }
        }

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_CULL_FACE);

    }


}
