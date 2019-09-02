package mcjty.rftools.blocks.screens;

import com.mojang.blaze3d.platform.GlStateManager;
import mcjty.lib.font.TrueTypeFont;
import mcjty.lib.varia.GlobalCoordinate;
import mcjty.rftools.RFTools;
import mcjty.rftools.api.screens.IClientScreenModule;
import mcjty.rftools.api.screens.ModuleRenderInfo;
import mcjty.rftools.api.screens.data.IModuleData;
import mcjty.rftools.blocks.screens.modulesclient.helper.ClientScreenModuleHelper;
import mcjty.rftools.blocks.screens.network.PacketGetScreenData;
import mcjty.rftools.network.RFToolsMessages;
import mcjty.rftools.setup.ClientProxy;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.opengl.GL11;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ScreenRenderer extends TileEntityRenderer<ScreenTileEntity> {

    private static final ResourceLocation texture = new ResourceLocation(RFTools.MODID, "textures/blocks/screenframe.png");
    private final ModelScreen screenModel = new ModelScreen(ScreenTileEntity.SIZE_NORMAL);
    private final ModelScreen screenModelLarge = new ModelScreen(ScreenTileEntity.SIZE_LARGE);
    private final ModelScreen screenModelHuge = new ModelScreen(ScreenTileEntity.SIZE_HUGE);

    @Override
    public void render(ScreenTileEntity tileEntity, double x, double y, double z, float partialTicks, int destroyStage) {
        float xRotation = 0.0F, yRotation = 0.0F;

        Direction facing = Direction.SOUTH, horizontalFacing = Direction.SOUTH;
        if (tileEntity != null) {
            BlockState state = Minecraft.getInstance().world.getBlockState(tileEntity.getPos());
            if (state.getBlock() instanceof ScreenBlock) {
                facing = state.get(BlockStateProperties.FACING);
                horizontalFacing = state.get(BlockStateProperties.HORIZONTAL_FACING);
            } else {
                return;
            }
        }

        GlStateManager.pushMatrix();

        switch (horizontalFacing) {
            case NORTH:
                yRotation = -180.0F;
                break;
            case WEST:
                yRotation = -90.0F;
                break;
            case EAST:
                yRotation = 90.0F;
        }
        switch (facing) {
            case DOWN:
                xRotation = 90.0F;
                break;
            case UP:
                xRotation = -90.0F;
        }

        // TileEntity can be null if this is used for an item renderer.
        GlStateManager.translatef((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);
        GlStateManager.rotatef(yRotation, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotatef(xRotation, 1.0F, 0.0F, 0.0F);
        GlStateManager.translatef(0.0F, 0.0F, -0.4375F);

        if (tileEntity == null) {
            GlStateManager.disableLighting();
            renderScreenBoard(0, 0);
        } else if (!tileEntity.isTransparent()) {
            GlStateManager.disableLighting();
            renderScreenBoard(tileEntity.getSize(), tileEntity.getColor());
        }

        if (tileEntity != null && tileEntity.isRenderable()) {
            FontRenderer fontrenderer = this.getFontRenderer();

            IClientScreenModule.TransformMode mode = IClientScreenModule.TransformMode.NONE;
            GlStateManager.depthMask(false);
            GlStateManager.disableLighting();

            Map<Integer, IModuleData> screenData = updateScreenData(tileEntity);

            List<IClientScreenModule<?>> modules = tileEntity.getClientScreenModules();
            if (tileEntity.isShowHelp()) {
                modules = ScreenTileEntity.getHelpingScreenModules();
            }
            renderModules(fontrenderer, tileEntity, mode, modules, screenData, tileEntity.getSize());
        }

        GlStateManager.enableLighting();
        GlStateManager.depthMask(true);

        GlStateManager.popMatrix();
    }

    private Map<Integer, IModuleData> updateScreenData(ScreenTileEntity screenTileEntity) {
        long millis = System.currentTimeMillis();
        if ((millis - screenTileEntity.lastTime > ScreenConfiguration.SCREEN_REFRESH_TIMING.get()) && screenTileEntity.isNeedsServerData()) {
            screenTileEntity.lastTime = millis;
            GlobalCoordinate pos = new GlobalCoordinate(screenTileEntity.getPos(), screenTileEntity.getWorld().getDimension().getType().getId());
            RFToolsMessages.INSTANCE.sendToServer(new PacketGetScreenData(RFTools.MODID, pos, millis));
        }

        GlobalCoordinate key = new GlobalCoordinate(screenTileEntity.getPos(), screenTileEntity.getWorld().getDimension().getType().getId());
        Map<Integer,IModuleData> screenData = ScreenTileEntity.screenData.get(key);
        if (screenData == null) {
            screenData = Collections.emptyMap();
        }
        return screenData;
    }

    private ClientScreenModuleHelper clientScreenModuleHelper = new ClientScreenModuleHelper();

    private void renderModules(FontRenderer fontrenderer, ScreenTileEntity tileEntity, IClientScreenModule.TransformMode mode, List<IClientScreenModule<?>> modules, Map<Integer, IModuleData> screenData, int size) {
        float f3;
        float factor = size + 1.0f;
        int currenty = 7;
        int moduleIndex = 0;

        BlockPos pos = tileEntity.getPos();

        RayTraceResult mouseOver = Minecraft.getInstance().objectMouseOver;
        IClientScreenModule<?> hitModule = null;
        ScreenTileEntity.ModuleRaytraceResult hit = null;
        BlockState blockState = getWorld().getBlockState(pos);
        Block block = blockState.getBlock();
        if (block != ScreenSetup.screenBlock && block != ScreenSetup.creativeScreenBlock && block != ScreenSetup.screenHitBlock) {
            // Safety
            return;
        }
        if (mouseOver instanceof BlockRayTraceResult) {
            Direction sideHit = ((BlockRayTraceResult) mouseOver).getFace();
            if (sideHit == blockState.get(BlockStateProperties.FACING)) {
                double xx = mouseOver.getHitVec().x - pos.getX();
                double yy = mouseOver.getHitVec().y - pos.getY();
                double zz = mouseOver.getHitVec().z - pos.getZ();
                Direction horizontalFacing = blockState.get(BlockStateProperties.HORIZONTAL_FACING);
                hit = tileEntity.getHitModule(xx, yy, zz, sideHit, horizontalFacing);
                if (hit != null) {
                    hitModule = modules.get(hit.getModuleIndex());
                }
                if (RFTools.setup.top) {
                    tileEntity.focusModuleClient(xx, yy, zz, sideHit, horizontalFacing);
                }
            }
        }

        if (tileEntity.isBright()) {
            Minecraft.getInstance().gameRenderer.disableLightmap();
        }

        for (IClientScreenModule module : modules) {
            if (module != null) {
                int height = module.getHeight();
                // Check if this module has enough room
                if (currenty + height <= 124) {
                    if (module.getTransformMode() != mode) {
                        if (mode != IClientScreenModule.TransformMode.NONE) {
                            GlStateManager.popMatrix();
                        }
                        GlStateManager.pushMatrix();
                        mode = module.getTransformMode();

                        switch (mode) {
                            case TEXT:
                                GlStateManager.translatef(-0.5F, 0.5F, 0.07F);
                                f3 = 0.0075F;
                                GlStateManager.scalef(f3 * factor, -f3 * factor, f3);
                                GL11.glNormal3f(0.0F, 0.0F, -1.0F);
                                GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                                break;
                            case TEXTLARGE:
                                GlStateManager.translatef(-0.5F, 0.5F, 0.07F);
                                f3 = 0.0075F * 2;
                                GlStateManager.scalef(f3 * factor, -f3 * factor, f3);
                                GL11.glNormal3f(0.0F, 0.0F, -1.0F);
                                GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                                break;
                            case ITEM:
                                break;
                            default:
                                break;
                        }
                    }

                    IModuleData data = screenData.get(moduleIndex);
                    // @todo this is a bit clumsy way to check if the data is compatible with the given module:
                    try {
                        int hitx = -1;
                        int hity = -1;
                        if (module == hitModule) {
                            hitx = hit.getX();
                            hity = hit.getY() - hit.getCurrenty();
                        }
                        TrueTypeFont font = null;
                        switch (tileEntity.getTrueTypeMode()) {
                            case -1: break;
                            case 1: font = ClientProxy.font; break;
                            case 0: font = ScreenConfiguration.useTruetype.get() ? ClientProxy.font : null; break;
                        }
                        ModuleRenderInfo renderInfo = new ModuleRenderInfo(factor, pos, hitx, hity, font);
                        module.render(clientScreenModuleHelper, fontrenderer, currenty, data, renderInfo);
                    } catch (ClassCastException e) {
                    }
                    currenty += height;
                }
            }
            moduleIndex++;
        }

        if (tileEntity.isBright()) {
            Minecraft.getInstance().gameRenderer.enableLightmap();
        }

        if (mode != IClientScreenModule.TransformMode.NONE) {
            GlStateManager.popMatrix();
        }
    }

    private void renderScreenBoard(int size, int color) {
        this.bindTexture(texture);
        GlStateManager.pushMatrix();
        GlStateManager.scalef(1, -1, -1);
        if (size == ScreenTileEntity.SIZE_HUGE) {
            this.screenModelHuge.render();
        } else if (size == ScreenTileEntity.SIZE_LARGE) {
            this.screenModelLarge.render();
        } else {
            this.screenModel.render();
        }

        GlStateManager.depthMask(false);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder renderer = tessellator.getBuffer();
        renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        float dim;
        if (size == ScreenTileEntity.SIZE_HUGE) {
            dim = 2.46f;
        } else if (size == ScreenTileEntity.SIZE_LARGE) {
            dim = 1.46f;
        } else {
            dim = .46f;
        }
        float r = ((color & 16711680) >> 16) / 255.0F;
        float g = ((color & 65280) >> 8) / 255.0F;
        float b = ((color & 255)) / 255.0F;
        renderer.pos(-.46f, dim, -0.08f).color(r, g, b, 1f).endVertex();
        renderer.pos(dim, dim, -0.08f).color(r, g, b, 1f).endVertex();
        renderer.pos(dim, -.46f, -0.08f).color(r, g, b, 1f).endVertex();
        renderer.pos(-.46f, -.46f, -0.08f).color(r, g, b, 1f).endVertex();
        tessellator.draw();

        GlStateManager.popMatrix();
    }

    public static void register() {
        ClientRegistry.bindTileEntitySpecialRenderer(ScreenTileEntity.class, new ScreenRenderer());
    }
}
