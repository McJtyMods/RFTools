package mcjty.rftools;

import mcjty.lib.gui.RenderGlowEffect;
import mcjty.lib.varia.GlobalCoordinate;
import mcjty.rftools.blocks.blockprotector.BlockProtectorTileEntity;
import mcjty.rftools.blocks.builder.BuilderSetup;
import mcjty.rftools.items.ModItems;
import mcjty.rftools.items.builder.ShapeCardItem;
import mcjty.rftools.items.smartwrench.SmartWrenchItem;
import mcjty.rftools.items.smartwrench.SmartWrenchMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.HashSet;
import java.util.Set;

@SideOnly(Side.CLIENT)
public class RenderWorldLastEventHandler {


    public static void tick(RenderWorldLastEvent evt) {
        renderHilightedBlock(evt);
        renderProtectedBlocks(evt);
    }

    private static void renderProtectedBlocks(RenderWorldLastEvent evt) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP p = mc.thePlayer;
        ItemStack heldItem = p.getHeldItem(EnumHand.MAIN_HAND);
        if (heldItem == null) {
            return;
        }
        if (heldItem.getItem() == ModItems.smartWrenchItem) {
            if (SmartWrenchItem.getCurrentMode(heldItem) == SmartWrenchMode.MODE_SELECT) {
                GlobalCoordinate current = SmartWrenchItem.getCurrentBlock(heldItem);
                if (current != null) {
                    if (current.getDimension() == mc.theWorld.provider.getDimension()) {
                        TileEntity te = mc.theWorld.getTileEntity(current.getCoordinate());
                        if (te instanceof BlockProtectorTileEntity) {
                            BlockProtectorTileEntity blockProtectorTileEntity = (BlockProtectorTileEntity) te;
                            Set<BlockPos> coordinates = blockProtectorTileEntity.getProtectedBlocks();
                            if (!coordinates.isEmpty()) {
                                renderHighlightedBlocks(evt, p, te.getPos(), coordinates);
                            }
                        }
                    }
                }
            }
        } else if (heldItem.getItem() == BuilderSetup.shapeCardItem) {
            int mode = ShapeCardItem.getMode(heldItem);
            if (mode == ShapeCardItem.MODE_CORNER1 || mode == ShapeCardItem.MODE_CORNER2) {
                GlobalCoordinate current = ShapeCardItem.getCurrentBlock(heldItem);
                if (current != null && current.getDimension() == mc.theWorld.provider.getDimension()) {
                    Set<BlockPos> coordinates = new HashSet<>();
                    coordinates.add(new BlockPos(0, 0, 0));
                    if (mode == ShapeCardItem.MODE_CORNER2) {
                        BlockPos cur = current.getCoordinate();
                        BlockPos c = ShapeCardItem.getCorner1(heldItem);
                        if (c != null) {
                            coordinates.add(new BlockPos(c.getX() - cur.getX(), c.getY() - cur.getY(), c.getZ() - cur.getZ()));
                        }
                    }
                    renderHighlightedBlocks(evt, p, current.getCoordinate(), coordinates);
                }
            }
        }
    }

    private static final ResourceLocation yellowglow = new ResourceLocation(RFTools.MODID, "textures/blocks/yellowglow.png");

    private static void renderHighlightedBlocks(RenderWorldLastEvent evt, EntityPlayerSP p, BlockPos base, Set<BlockPos> coordinates) {
        double doubleX = p.lastTickPosX + (p.posX - p.lastTickPosX) * evt.getPartialTicks();
        double doubleY = p.lastTickPosY + (p.posY - p.lastTickPosY) * evt.getPartialTicks();
        double doubleZ = p.lastTickPosZ + (p.posZ - p.lastTickPosZ) * evt.getPartialTicks();

        GlStateManager.pushMatrix();
        GlStateManager.translate(-doubleX, -doubleY, -doubleZ);

        GlStateManager.disableDepth();
        GlStateManager.enableTexture2D();

        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer buffer = tessellator.getBuffer();

        Minecraft.getMinecraft().getTextureManager().bindTexture(yellowglow);

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);
//        tessellator.setColorRGBA(255, 255, 255, 64);
//        tessellator.setBrightness(240);

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        for (BlockPos coordinate : coordinates) {
            float x = base.getX() + coordinate.getX();
            float y = base.getY() + coordinate.getY();
            float z = base.getZ() + coordinate.getZ();
            buffer.setTranslation(buffer.xOffset + x, buffer.yOffset + y, buffer.zOffset + z);

            RenderGlowEffect.addSideFullTexture(buffer, EnumFacing.UP.ordinal(), 1.1f, -0.05f);
            RenderGlowEffect.addSideFullTexture(buffer, EnumFacing.DOWN.ordinal(), 1.1f, -0.05f);
            RenderGlowEffect.addSideFullTexture(buffer, EnumFacing.NORTH.ordinal(), 1.1f, -0.05f);
            RenderGlowEffect.addSideFullTexture(buffer, EnumFacing.SOUTH.ordinal(), 1.1f, -0.05f);
            RenderGlowEffect.addSideFullTexture(buffer, EnumFacing.WEST.ordinal(), 1.1f, -0.05f);
            RenderGlowEffect.addSideFullTexture(buffer, EnumFacing.EAST.ordinal(), 1.1f, -0.05f);
            buffer.setTranslation(buffer.xOffset - x, buffer.yOffset - y, buffer.zOffset - z);
        }
        tessellator.draw();

        GlStateManager.disableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.color(.5f, .3f, 0);
        GlStateManager.glLineWidth(2);

        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);

        for (BlockPos coordinate : coordinates) {
            renderHighLightedBlocksOutline(buffer, base.getX() + coordinate.getX(), base.getY() + coordinate.getY(), base.getZ() + coordinate.getZ());
        }
        tessellator.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

    private static void renderHighLightedBlocksOutline(VertexBuffer buffer, float mx, float my, float mz) {
        buffer.pos(mx, my, mz).endVertex();
        buffer.pos(mx+1, my, mz).endVertex();
        buffer.pos(mx, my, mz).endVertex();
        buffer.pos(mx, my+1, mz).endVertex();
        buffer.pos(mx, my, mz).endVertex();
        buffer.pos(mx, my, mz+1).endVertex();
        buffer.pos(mx+1, my+1, mz+1).endVertex();
        buffer.pos(mx, my+1, mz+1).endVertex();
        buffer.pos(mx+1, my+1, mz+1).endVertex();
        buffer.pos(mx+1, my, mz+1).endVertex();
        buffer.pos(mx+1, my+1, mz+1).endVertex();
        buffer.pos(mx+1, my+1, mz).endVertex();

        buffer.pos(mx, my+1, mz).endVertex();
        buffer.pos(mx, my+1, mz+1).endVertex();
        buffer.pos(mx, my+1, mz).endVertex();
        buffer.pos(mx+1, my+1, mz).endVertex();

        buffer.pos(mx+1, my, mz).endVertex();
        buffer.pos(mx+1, my, mz+1).endVertex();
        buffer.pos(mx+1, my, mz).endVertex();
        buffer.pos(mx+1, my+1, mz).endVertex();

        buffer.pos(mx, my, mz+1).endVertex();
        buffer.pos(mx+1, my, mz+1).endVertex();
        buffer.pos(mx, my, mz+1).endVertex();
        buffer.pos(mx, my+1, mz+1).endVertex();
    }

    private static void renderHilightedBlock(RenderWorldLastEvent evt) {
        BlockPos c = RFTools.instance.clientInfo.getHilightedBlock();
        if (c == null) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        long time = System.currentTimeMillis();

        if (time > RFTools.instance.clientInfo.getExpireHilight()) {
            RFTools.instance.clientInfo.hilightBlock(null, -1);
            return;
        }

        if (((time / 500) & 1) == 0) {
            return;
        }

        EntityPlayerSP p = mc.thePlayer;
        double doubleX = p.lastTickPosX + (p.posX - p.lastTickPosX) * evt.getPartialTicks();
        double doubleY = p.lastTickPosY + (p.posY - p.lastTickPosY) * evt.getPartialTicks();
        double doubleZ = p.lastTickPosZ + (p.posZ - p.lastTickPosZ) * evt.getPartialTicks();

        GlStateManager.pushMatrix();
        GlStateManager.color(1.0f, 0, 0);
        GlStateManager.glLineWidth(3);
        GlStateManager.translate(-doubleX, -doubleY, -doubleZ);

        GlStateManager.disableDepth();
        GlStateManager.disableTexture2D();

        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer buffer = tessellator.getBuffer();
        float mx = c.getX();
        float my = c.getY();
        float mz = c.getZ();
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
        renderHighLightedBlocksOutline(buffer, mx, my, mz);

        tessellator.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }
}
