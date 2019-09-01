package mcjty.rftools.blocks.elevator;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.opengl.GL11;

public class ElevatorTESR extends TileEntityRenderer<ElevatorTileEntity> {

    private static final BlockRenderLayer[] LAYERS = BlockRenderLayer.values();

    private final FakeElevatorWorld fakeWorld = new FakeElevatorWorld();

    @Override
    public void render(ElevatorTileEntity te, double x, double y, double z, float partialTicks, int destroyStage) {

        if (te.isMoving()) {
            // Correction in the y translation to avoid jitter when both player and platform are moving
            AxisAlignedBB aabb = te.getAABBAboveElevator(0);
            boolean on = Minecraft.getInstance().player.getBoundingBox().intersects(aabb);

            double diff = on ? (te.getPos().getY() - (y+te.getMovingY()) - 1) : 0;

            GlStateManager.pushMatrix();

            RenderHelper.disableStandardItemLighting();
            this.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
            if (Minecraft.isAmbientOcclusionEnabled()) {
                GlStateManager.shadeModel(GL11.GL_SMOOTH);
            } else {
                GlStateManager.shadeModel(GL11.GL_FLAT);
            }

            BlockState movingState = te.getMovingState();

            GlStateManager.translatef(0f, (float) (te.getMovingY() - te.getPos().getY() + diff), 0f);
            Tessellator tessellator = Tessellator.getInstance();
            BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
            BlockRenderLayer origLayer = MinecraftForgeClient.getRenderLayer();

            fakeWorld.setWorldAndState(te);

            for (BlockRenderLayer layer : LAYERS) {
                if (movingState.getBlock().canRenderInLayer(movingState, layer)) {
                    ForgeHooksClient.setRenderLayer(layer);
                    if (layer == BlockRenderLayer.TRANSLUCENT) {
                        GlStateManager.enableBlend();
                    }

                    for (BlockPos pos : te.getPositions()) {
                        tessellator.getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
                        int dx = te.getPos().getX() - pos.getX();
                        int dy = te.getPos().getY() - pos.getY();
                        int dz = te.getPos().getZ() - pos.getZ();
                        tessellator.getBuffer().setTranslation(x - pos.getX() - dx, y - pos.getY() - dy, z - pos.getZ() - dz);
                        renderBlock(dispatcher, movingState, pos, fakeWorld, tessellator.getBuffer());
                        tessellator.draw();
                    }

                    if (layer == BlockRenderLayer.TRANSLUCENT) {
                        GlStateManager.disableBlend();
                    }
                }
            }

            ForgeHooksClient.setRenderLayer(origLayer);
            tessellator.getBuffer().setTranslation(0, 0, 0);

            RenderHelper.enableStandardItemLighting();
            GlStateManager.popMatrix();
        }
    }

    @Override
    public boolean isGlobalRenderer(ElevatorTileEntity te) {
        return te.isMoving();
    }

    private static boolean renderBlock(BlockRendererDispatcher dispatcher, BlockState state, BlockPos pos, IBlockReader blockAccess, BufferBuilder worldRendererIn) {
        try {
            BlockRenderType enumblockrendertype = state.getRenderType();

            if (enumblockrendertype == BlockRenderType.INVISIBLE) {
                return false;
            } else {
                if (((World)blockAccess).getWorldType() != WorldType.DEBUG_ALL_BLOCK_STATES) {
                    try {
                        state = state.getExtendedState(blockAccess, pos);   // @todo 1.14 is this right?
//                        state = state.getActualState(blockAccess, pos);
                    } catch (Exception var8) {
                    }
                }

                switch (enumblockrendertype) {
                    case MODEL:
                        IBakedModel model = dispatcher.getModelForState(state);
                        state = state.getBlock().getExtendedState(state, blockAccess, pos);
                        return dispatcher.getBlockModelRenderer().renderModel((World)blockAccess, model, state, pos, worldRendererIn, false, ((World) blockAccess).rand, 0 /*1.4 what?*/);  // @todo 1.14 check?
                    case ENTITYBLOCK_ANIMATED:
                        return false;
                    default:
                        return false;
                }
            }
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Tesselating block in world");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Block being tesselated");
            CrashReportCategory.addBlockInfo(crashreportcategory, pos, state);
            throw new ReportedException(crashreport);
        }
    }

    public static void register() {
        ClientRegistry.bindTileEntitySpecialRenderer(ElevatorTileEntity.class, new ElevatorTESR());
    }
}
