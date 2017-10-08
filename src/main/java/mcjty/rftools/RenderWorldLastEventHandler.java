package mcjty.rftools;

import mcjty.lib.api.information.IMachineInformation;
import mcjty.lib.api.smartwrench.SmartWrenchMode;
import mcjty.lib.gui.HudRenderHelper;
import mcjty.lib.gui.RenderGlowEffect;
import mcjty.lib.tools.ItemStackTools;
import mcjty.lib.tools.MinecraftTools;
import mcjty.lib.varia.GlobalCoordinate;
import mcjty.rftools.blocks.blockprotector.BlockProtectorTileEntity;
import mcjty.rftools.blocks.builder.BuilderSetup;
import mcjty.rftools.items.ModItems;
import mcjty.rftools.items.builder.ShapeCardItem;
import mcjty.rftools.items.netmonitor.NetworkMonitorItem;
import mcjty.rftools.items.smartwrench.SmartWrenchItem;
import mcjty.rftools.network.PacketGetRfInRange;
import mcjty.rftools.network.PacketReturnRfInRange;
import mcjty.rftools.network.RFToolsMessages;
import mcjty.rftools.shapes.ShapeDataManagerClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.*;

@SideOnly(Side.CLIENT)
public class RenderWorldLastEventHandler {

    private static long lastTime = 0;

    public static void tick(RenderWorldLastEvent evt) {
        renderHilightedBlock(evt);
        renderProtectedBlocks(evt);
        renderPower(evt);
        ShapeDataManagerClient.cleanupOldRenderers();
    }

    private static void renderProtectedBlocks(RenderWorldLastEvent evt) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP p = MinecraftTools.getPlayer(mc);
        ItemStack heldItem = p.getHeldItem(EnumHand.MAIN_HAND);
        if (ItemStackTools.isEmpty(heldItem)) {
            return;
        }
        if (heldItem.getItem() == ModItems.smartWrenchItem) {
            if (SmartWrenchItem.getCurrentMode(heldItem) == SmartWrenchMode.MODE_SELECT) {
                GlobalCoordinate current = SmartWrenchItem.getCurrentBlock(heldItem);
                if (current != null) {
                    if (current.getDimension() == MinecraftTools.getWorld(mc).provider.getDimension()) {
                        TileEntity te = MinecraftTools.getWorld(mc).getTileEntity(current.getCoordinate());
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
                if (current != null && current.getDimension() == MinecraftTools.getWorld(mc).provider.getDimension()) {
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

    public static final ResourceLocation YELLOWGLOW = new ResourceLocation(RFTools.MODID, "textures/blocks/yellowglow.png");

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

        Minecraft.getMinecraft().getTextureManager().bindTexture(YELLOWGLOW);

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

        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

        for (BlockPos coordinate : coordinates) {
            mcjty.lib.gui.RenderHelper.renderHighLightedBlocksOutline(buffer,
                    base.getX() + coordinate.getX(), base.getY() + coordinate.getY(), base.getZ() + coordinate.getZ(),
                    .5f, .3f, 0f, 1.0f);
        }
        tessellator.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
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

        EntityPlayerSP p = MinecraftTools.getPlayer(mc);
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
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        mcjty.lib.gui.RenderHelper.renderHighLightedBlocksOutline(buffer, mx, my, mz, 1.0f, 0.0f, 0.0f, 1.0f);

        tessellator.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

    private static void renderPower(RenderWorldLastEvent evt) {
        EntityPlayerSP player = MinecraftTools.getPlayer(Minecraft.getMinecraft());

        ItemStack mainItem = player.getHeldItemMainhand();
        ItemStack offItem = player.getHeldItemOffhand();
        if ((ItemStackTools.isValid(mainItem) && mainItem.getItem() instanceof NetworkMonitorItem)
                || (ItemStackTools.isValid(offItem) && offItem.getItem() instanceof NetworkMonitorItem)) {
            double doubleX = player.lastTickPosX + (player.posX - player.lastTickPosX) * evt.getPartialTicks();
            double doubleY = player.lastTickPosY + (player.posY - player.lastTickPosY) * evt.getPartialTicks();
            double doubleZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * evt.getPartialTicks();

            GlStateManager.pushMatrix();
            GlStateManager.translate(-doubleX, -doubleY, -doubleZ);

            GlStateManager.disableDepth();
            GlStateManager.enableTexture2D();

            if (System.currentTimeMillis() - lastTime > 500) {
                lastTime = System.currentTimeMillis();
                RFToolsMessages.INSTANCE.sendToServer(new PacketGetRfInRange(player.getPosition()));
            }

            if (PacketReturnRfInRange.clientLevels == null) {
                return;
            }
            for (Map.Entry<BlockPos, PacketGetRfInRange.MachineInfo> entry : PacketReturnRfInRange.clientLevels.entrySet()) {
                BlockPos pos = entry.getKey();
                List<String> log = new ArrayList<>();
                PacketGetRfInRange.MachineInfo info = entry.getValue();
                log.add(TextFormatting.BLUE + "RF:  " + TextFormatting.WHITE + info.getEnergy());
                log.add(TextFormatting.BLUE + "Max: " + TextFormatting.WHITE + info.getMaxEnergy());
                if (info.getEnergyPerTick() != null) {
                    TileEntity te = player.getEntityWorld().getTileEntity(pos);
                    String unit = "";
                    if (te instanceof IMachineInformation) {
                        unit = ((IMachineInformation) te).getEnergyUnitName();
                        if (unit == null) {
                            unit = "";
                        }
                    }
                    int usage = info.getEnergyPerTick();
                    if (usage < 0) {
                        log.add(TextFormatting.RED + "" + usage + unit + "/t");
                    } else if (usage > 0) {
                        log.add(TextFormatting.GREEN + "" + usage + unit + "/t");
                    }
                }

                HudRenderHelper.renderHud(log, HudRenderHelper.HudPlacement.HUD_CENTER, HudRenderHelper.HudOrientation.HUD_TOPLAYER,
                        null, pos.getX(), pos.getY(), pos.getZ(), 2.0f);
                renderBoxOutline(pos);
            }

            GlStateManager.enableDepth();

            GlStateManager.popMatrix();
        }
    }

    private static void renderBoxOutline(BlockPos pos) {
        BlockPos c = RFTools.instance.clientInfo.getHilightedBlock();
        if (c != null && c.equals(pos)) {
            return;
        }

        RenderHelper.disableStandardItemLighting();
        Minecraft.getMinecraft().entityRenderer.disableLightmap();
        GlStateManager.disableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.disableLighting();
        GlStateManager.disableAlpha();
        GlStateManager.glLineWidth(2);
        GlStateManager.color(1, 1, 1);

        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer buffer = tessellator.getBuffer();
        float mx = pos.getX();
        float my = pos.getY();
        float mz = pos.getZ();
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        mcjty.lib.gui.RenderHelper.renderHighLightedBlocksOutline(buffer, mx, my, mz, .9f, .7f, 0, 1);

        tessellator.draw();

        Minecraft.getMinecraft().entityRenderer.enableLightmap();
        GlStateManager.enableTexture2D();
    }
}
