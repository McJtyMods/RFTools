package mcjty.rftools.blocks.elevator;

import mcjty.lib.tools.MinecraftTools;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraftforge.client.ForgeHooksClient;
import org.lwjgl.opengl.GL11;

public class ElevatorTESR extends TileEntitySpecialRenderer<ElevatorTileEntity> {

    private static final BlockRenderLayer[] LAYERS = BlockRenderLayer.values();

    @Override
    public void renderTileEntityAt(ElevatorTileEntity te, double x, double y, double z, float partialTicks, int destroyStage) {

        if (te.isMoving()) {
            // Correction in the y translation to avoid jitter when both player and platform are moving
            AxisAlignedBB aabb = te.getAABBAboveElevator(0);
            boolean on = MinecraftTools.getPlayer(Minecraft.getMinecraft()).getEntityBoundingBox().intersectsWith(aabb);

            double diff = on ? (te.getPos().getY() - (y+te.getMovingY()) - 1) : 0;

            GlStateManager.pushMatrix();

            RenderHelper.disableStandardItemLighting();
            this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            if (Minecraft.isAmbientOcclusionEnabled()) {
                GlStateManager.shadeModel(GL11.GL_SMOOTH);
            } else {
                GlStateManager.shadeModel(GL11.GL_FLAT);
            }

            IBlockState movingState = te.getMovingState();

            GlStateManager.translate(0, te.getMovingY() - te.getPos().getY() + diff, 0);
            Tessellator tessellator = Tessellator.getInstance();
            BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();

            for (BlockRenderLayer layer : LAYERS) {
                if (movingState.getBlock().canRenderInLayer(movingState, layer)) {
                    ForgeHooksClient.setRenderLayer(layer);

                    for (BlockPos pos : te.getPositions()) {
                        tessellator.getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
                        int dx = te.getPos().getX() - pos.getX();
                        int dy = te.getPos().getY() - pos.getY();
                        int dz = te.getPos().getZ() - pos.getZ();
                        tessellator.getBuffer().setTranslation(x - pos.getX() - dx, y - pos.getY() - dy, z - pos.getZ() - dz);
                        renderBlock(dispatcher, movingState, pos, te.getWorld(), tessellator.getBuffer());
                        tessellator.draw();
                    }
                }
            }

            tessellator.getBuffer().setTranslation(0, 0, 0);

            RenderHelper.enableStandardItemLighting();
            GlStateManager.popMatrix();
        }
    }

    @Override
    public boolean isGlobalRenderer(ElevatorTileEntity te) {
        return te.isMoving();
    }

    private static boolean renderBlock(BlockRendererDispatcher dispatcher, IBlockState state, BlockPos pos, IBlockAccess blockAccess, VertexBuffer worldRendererIn) {
        try {
            EnumBlockRenderType enumblockrendertype = state.getRenderType();

            if (enumblockrendertype == EnumBlockRenderType.INVISIBLE) {
                return false;
            } else {
                if (blockAccess.getWorldType() != WorldType.DEBUG_WORLD) {
                    try {
                        state = state.getActualState(blockAccess, pos);
                    } catch (Exception var8) {
                    }
                }

                switch (enumblockrendertype) {
                    case MODEL:
                        IBakedModel model = dispatcher.getModelForState(state);
                        state = state.getBlock().getExtendedState(state, blockAccess, pos);
                        return dispatcher.getBlockModelRenderer().renderModel(blockAccess, model, state, pos, worldRendererIn, false);
                    case ENTITYBLOCK_ANIMATED:
                        return false;
                    default:
                        return false;
                }
            }
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Tesselating block in world");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Block being tesselated");
            CrashReportCategory.addBlockInfo(crashreportcategory, pos, state.getBlock(), state.getBlock().getMetaFromState(state));
            throw new ReportedException(crashreport);
        }
    }


}
