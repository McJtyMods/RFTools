package mcjty.rftools;

import com.mojang.blaze3d.platform.GlStateManager;
import mcjty.lib.api.information.IMachineInformation;
import mcjty.lib.api.smartwrench.SmartWrenchMode;
import mcjty.lib.client.BlockOutlineRenderer;
import mcjty.lib.client.HudRenderHelper;
import mcjty.lib.varia.GlobalCoordinate;
import mcjty.rftools.blocks.blockprotector.BlockProtectorConfiguration;
import mcjty.rftools.blocks.blockprotector.BlockProtectorTileEntity;
import mcjty.rftools.blocks.builder.BuilderSetup;
import mcjty.rftools.blocks.builder.BuilderTileEntity;
import mcjty.rftools.items.builder.ShapeCardItem;
import mcjty.rftools.items.netmonitor.NetworkMonitorItem;
import mcjty.rftools.network.MachineInfo;
import mcjty.rftools.network.PacketReturnRfInRange;
import mcjty.rftools.network.RFToolsMessages;
import mcjty.rftools.setup.CommandHandler;
import mcjty.rftools.shapes.ShapeDataManagerClient;
import mcjty.rftoolsbase.items.SmartWrenchItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import java.util.*;

public class RenderWorldLastEventHandler {

    private static long lastTime = 0;

    public static void tick(RenderWorldLastEvent evt) {
        renderHilightedBlock(evt);
        renderBuilderProgress(evt);
        renderProtectedBlocks(evt);
        renderPower(evt);
        ShapeDataManagerClient.cleanupOldRenderers();
    }

    private static void renderProtectedBlocks(RenderWorldLastEvent evt) {
        Minecraft mc = Minecraft.getInstance();
        PlayerEntity p = mc.player;
        ItemStack heldItem = p.getHeldItem(Hand.MAIN_HAND);
        if (heldItem.isEmpty()) {
            return;
        }
        if (heldItem.getItem() instanceof SmartWrenchItem) {
            if (BlockProtectorConfiguration.enabled.get() && SmartWrenchItem.getCurrentMode(heldItem) == SmartWrenchMode.MODE_SELECT) {
                GlobalCoordinate current = SmartWrenchItem.getCurrentBlock(heldItem);
                if (current != null) {
                    if (current.getDimension() == mc.world.getDimension().getType().getId()) {
                        TileEntity te = mc.world.getTileEntity(current.getCoordinate());
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
                if (current != null && current.getDimension() == mc.world.getDimension().getType().getId()) {
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

    private static void renderHighlightedBlocks(RenderWorldLastEvent evt, PlayerEntity p, BlockPos base, Set<BlockPos> coordinates) {
        BlockOutlineRenderer.renderHighlightedBlocks(p, base, coordinates, YELLOWGLOW, evt.getPartialTicks());
    }

    private static void renderBuilderProgress(RenderWorldLastEvent evt) {
        Map<BlockPos, Pair<Long, BlockPos>> scans = BuilderTileEntity.getScanLocClient();
        if (!scans.isEmpty()) {
            Minecraft mc = Minecraft.getInstance();
            PlayerEntity p = mc.player;
            double doubleX = p.lastTickPosX + (p.posX - p.lastTickPosX) * evt.getPartialTicks();
            double doubleY = p.lastTickPosY + (p.posY - p.lastTickPosY) * evt.getPartialTicks();
            double doubleZ = p.lastTickPosZ + (p.posZ - p.lastTickPosZ) * evt.getPartialTicks();

            GlStateManager.pushMatrix();
            GlStateManager.color3f(1.0f, 0, 0);
            GlStateManager.lineWidth(3);
            GlStateManager.translated(-doubleX, -doubleY, -doubleZ);

            GlStateManager.disableDepthTest();
            GlStateManager.disableTexture();

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

            for (Map.Entry<BlockPos, Pair<Long, BlockPos>> entry : scans.entrySet()) {
                BlockPos c = entry.getValue().getValue();
                float mx = c.getX();
                float my = c.getY();
                float mz = c.getZ();
                mcjty.lib.client.RenderHelper.renderHighLightedBlocksOutline(buffer, mx, my, mz, 0.0f, 1.0f, 1.0f, 1.0f);
            }

            tessellator.draw();

            GlStateManager.enableTexture();
            GlStateManager.popMatrix();
        }
    }

    private static void renderHilightedBlock(RenderWorldLastEvent evt) {
        BlockPos c = RFTools.instance.clientInfo.getHilightedBlock();
        if (c == null) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        long time = System.currentTimeMillis();

        if (time > RFTools.instance.clientInfo.getExpireHilight()) {
            RFTools.instance.clientInfo.hilightBlock(null, -1);
            return;
        }

        if (((time / 500) & 1) == 0) {
            return;
        }

        BlockOutlineRenderer.renderHilightedBlock(c, evt.getPartialTicks());
    }

    private static void renderPower(RenderWorldLastEvent evt) {
        PlayerEntity player = Minecraft.getInstance().player;

        ItemStack mainItem = player.getHeldItemMainhand();
        ItemStack offItem = player.getHeldItemOffhand();
        if ((!mainItem.isEmpty() && mainItem.getItem() instanceof NetworkMonitorItem)
                || (!offItem.isEmpty() && offItem.getItem() instanceof NetworkMonitorItem)) {
            double doubleX = player.lastTickPosX + (player.posX - player.lastTickPosX) * evt.getPartialTicks();
            double doubleY = player.lastTickPosY + (player.posY - player.lastTickPosY) * evt.getPartialTicks();
            double doubleZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * evt.getPartialTicks();

            GlStateManager.pushMatrix();
            GlStateManager.translated(-doubleX, -doubleY, -doubleZ);

            GlStateManager.disableDepthTest();
            GlStateManager.enableTexture();

            if (System.currentTimeMillis() - lastTime > 500) {
                lastTime = System.currentTimeMillis();
                RFToolsMessages.sendToServer(CommandHandler.CMD_GET_RF_IN_RANGE);
            }

            if (PacketReturnRfInRange.clientLevels == null) {
                return;
            }
            for (Map.Entry<BlockPos, MachineInfo> entry : PacketReturnRfInRange.clientLevels.entrySet()) {
                BlockPos pos = entry.getKey();
                List<String> log = new ArrayList<>();
                MachineInfo info = entry.getValue();
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
                    long usage = info.getEnergyPerTick();
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

            GlStateManager.enableDepthTest();

            GlStateManager.popMatrix();
        }
    }

    private static void renderBoxOutline (BlockPos pos) {
        BlockPos c = RFTools.instance.clientInfo.getHilightedBlock();
        if (c != null && c.equals(pos)) {
            return;
        }
        BlockOutlineRenderer.renderBoxOutline(pos);
    }
}
