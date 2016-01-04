package mcjty.rftools.blocks.screens;

import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.screens.modulesclient.ClientScreenModule;
import mcjty.rftools.blocks.screens.network.PacketGetScreenData;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class ScreenRenderer extends TileEntitySpecialRenderer<ScreenTileEntity> {

    private static final ResourceLocation texture = new ResourceLocation(RFTools.MODID, "textures/blocks/screenFrame.png");
    private final ModelScreen screenModel = new ModelScreen(ScreenTileEntity.SIZE_NORMAL);
    private final ModelScreen screenModelLarge = new ModelScreen(ScreenTileEntity.SIZE_LARGE);
    private final ModelScreen screenModelHuge = new ModelScreen(ScreenTileEntity.SIZE_HUGE);

    @Override
    public void renderTileEntityAt(ScreenTileEntity tileEntity, double x, double y, double z, float partialTicks, int destroyStage) {
//        GL11.glPushAttrib(GL11.GL_CURRENT_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_ENABLE_BIT | GL11.GL_LIGHTING_BIT | GL11.GL_TEXTURE_BIT);
        GlStateManager.pushAttrib();
        GlStateManager.pushMatrix();
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

        GlStateManager.translate((float) x + 0.5F, (float) y + 0.75F, (float) z + 0.5F);
        GlStateManager.rotate(-f3, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(0.0F, -0.2500F, -0.4375F);

        if (!tileEntity.isTransparent()) {
            GlStateManager.disableLighting();
            renderScreenBoard(tileEntity.getSize(), tileEntity.getColor());
        }

        if (tileEntity.isPowerOn()) {
            FontRenderer fontrenderer = this.getFontRenderer();

            ClientScreenModule.TransformMode mode = ClientScreenModule.TransformMode.NONE;
            GlStateManager.depthMask(false);
            GlStateManager.disableLighting();

            Map<Integer, Object[]> screenData = updateScreenData(tileEntity);

            List<ClientScreenModule> modules = tileEntity.getClientScreenModules();
            renderModules(fontrenderer, mode, modules, screenData, tileEntity.getSize());
        }

        GlStateManager.popMatrix();
        GlStateManager.popAttrib();
//        GL11.glPopAttrib();
    }

    private Map<Integer, Object[]> updateScreenData(ScreenTileEntity screenTileEntity) {
        long millis = System.currentTimeMillis();
        if ((millis - screenTileEntity.lastTime > 500) && screenTileEntity.isNeedsServerData()) {
            screenTileEntity.lastTime = millis;
            RFToolsMessages.INSTANCE.sendToServer(new PacketGetScreenData(RFTools.MODID, screenTileEntity.getPos(), millis));
        }

        Map<Integer,Object[]> screenData = ScreenTileEntity.screenData.get(screenTileEntity.getPos());
        if (screenData == null) {
            screenData = Collections.EMPTY_MAP;
        }
        return screenData;
    }

    private void renderModules(FontRenderer fontrenderer, ClientScreenModule.TransformMode mode, List<ClientScreenModule> modules, Map<Integer, Object[]> screenData, int size) {
        float f3;
        float factor = size + 1.0f;
        int currenty = 7;
        int moduleIndex = 0;
        for (ClientScreenModule module : modules) {
            if (module != null) {
                int height = module.getHeight();
                // Check if this module has enough room
                if (currenty + height <= 124) {
                    if (module.getTransformMode() != mode) {
                        if (mode != ClientScreenModule.TransformMode.NONE) {
                            GlStateManager.popMatrix();
                        }
                        GlStateManager.pushMatrix();
                        mode = module.getTransformMode();

                        switch (mode) {
                            case TEXT:
                                GlStateManager.translate(-0.5F, 0.5F, 0.07F);
                                f3 = 0.0075F;
                                GlStateManager.scale(f3 * factor, -f3 * factor, f3);
                                GL11.glNormal3f(0.0F, 0.0F, -1.0F);
                                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                                break;
                            case TEXTLARGE:
                                GlStateManager.translate(-0.5F, 0.5F, 0.07F);
                                f3 = 0.0075F * 2;
                                GlStateManager.scale(f3 * factor, -f3 * factor, f3);
                                GL11.glNormal3f(0.0F, 0.0F, -1.0F);
                                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                                break;
                            case ITEM:
                                break;
                            default:
                                break;
                        }
                    }

                    module.render(fontrenderer, currenty, screenData.get(moduleIndex), factor);
                    currenty += height;
                }
            }
            moduleIndex++;
        }

        if (mode != ClientScreenModule.TransformMode.NONE) {
            GlStateManager.popMatrix();
        }
    }

    private void renderScreenBoard(int size, int color) {
        this.bindTexture(texture);
        GlStateManager.pushMatrix();
        GlStateManager.scale(1, -1, -1);
        if (size == ScreenTileEntity.SIZE_HUGE) {
            this.screenModelHuge.render();
        } else if (size == ScreenTileEntity.SIZE_LARGE) {
            this.screenModelLarge.render();
        } else {
            this.screenModel.render();
        }

        GlStateManager.depthMask(false);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer renderer = tessellator.getWorldRenderer();
        renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        float dim;
        if (size == ScreenTileEntity.SIZE_HUGE) {
            dim = 2.46f;
        } else if (size == ScreenTileEntity.SIZE_LARGE) {
            dim = 1.46f;
        } else {
            dim = .46f;
        }
        int r = (color<<16)%255;
        int g = (color<<8)%255;
        int b = color%255;
        renderer.pos(-.46f, dim, -0.08f).color(r, g, b, 255).endVertex();
        renderer.pos(dim, dim, -0.08f).color(r, g, b, 255).endVertex();
        renderer.pos(dim, -.46f, -0.08f).color(r, g, b, 255).endVertex();
        renderer.pos(-.46f, -.46f, -0.08f).color(r, g, b, 255).endVertex();
        tessellator.draw();

        GlStateManager.popMatrix();
    }
}
