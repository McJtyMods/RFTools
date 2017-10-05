package mcjty.rftools.shapes;

import mcjty.lib.tools.ItemStackTools;
import mcjty.rftools.blocks.builder.BuilderSetup;
import mcjty.rftools.items.builder.ShapeCardItem;
import mcjty.rftools.network.RFToolsMessages;
import mcjty.rftools.varia.Check32;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShapeRenderer {

    private int prevX = -1;
    private int prevY = -1;

    private float scale = 3.0f;
    private float dx = 230.0f;
    private float dy = 100.0f;
    private float xangle = 25.0f;
    private float yangle = 25.0f;
    private float zangle = 0.0f;

    private ShapeID shapeID;

    private int waitForNewRequest = 0;


    public ShapeRenderer(ShapeID shapeID) {
        this.shapeID = shapeID;
    }

    public void setShapeID(ShapeID shapeID) {
        this.shapeID = shapeID;
    }

    public ShapeID getShapeID() {
        return shapeID;
    }

    public int getCount() {
        RenderData data = ShapeDataManager.getRenderData(shapeID);
        if (data != null) {
            return data.getBlockCount();
        }
        return 0;
    }

    public static RenderData getRenderDataAndCreate(ShapeID shapeID) {
        RenderData data = ShapeDataManager.getRenderDataAndCreate(shapeID);
        data.touch();
        return data;
    }

    public static void setRenderData(ShapeID id, @Nullable RenderData.RenderPlane plane, int offsetY, int dy, String msg) {
        RenderData data = getRenderDataAndCreate(id);
        data.setPlaneData(plane, offsetY, dy);
        data.previewMessage = msg;
    }

    public void initView(float dx, float dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public void handleShapeDragging(int x, int y) {
        if (x >= 100 && y <= 120) {
            if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
                if (prevX != -1 && Mouse.isButtonDown(0)) {
                    dx += (x - prevX);
                    dy += (y - prevY);
                }
            } else {
                if (prevX != -1 && Mouse.isButtonDown(0)) {
                    yangle -= (x - prevX);
                    xangle += (y - prevY);
                }
            }
            prevX = x;
            prevY = y;
        }

        if (Mouse.isButtonDown(2)) {
            xangle = 0.0f;
            yangle = 0.0f;
        }
    }

    public void handleMouseWheel() {
        int dwheel = Mouse.getDWheel();
        if (dwheel < 0) {
            scale *= .6;
            if (scale <= 0.1) {
                scale = .1f;
            }
        } else if (dwheel > 0) {
            scale *= 1.4;
        }
    }

    public void renderShapeInWorld(ItemStack stack, double x, double y, double z, float offset, float scale, float angle) {
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x + 0.5F, (float) y + 1F + offset, (float) z + 0.5F);
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.rotate(angle, 0, 1, 0);

        net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
        Minecraft.getMinecraft().entityRenderer.disableLightmap();
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.disableLighting();
        GlStateManager.disableTexture2D();

        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer buffer = tessellator.getBuffer();
        renderFaces(tessellator, buffer, stack);

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        RenderHelper.enableStandardItemLighting();
        Minecraft.getMinecraft().entityRenderer.enableLightmap();

        GlStateManager.popMatrix();
    }

    public void renderShape(IShapeParentGui gui, ItemStack stack, int x, int y, boolean showAxis, boolean showOuter, boolean showGuidelines) {
        setupScissor(gui);

        GlStateManager.pushMatrix();

        GlStateManager.translate(dx, dy, 200);
        GlStateManager.rotate(180-xangle, 1f, 0, 0); //xangle += .16f;
        GlStateManager.rotate(yangle, 0, 1f, 0); //yangle += .09f;
        GlStateManager.rotate(zangle, 0, 0, 1f); //zangle += .31f;
        GlStateManager.scale(scale, scale, scale);

        GlStateManager.disableBlend();
        GlStateManager.disableCull();
        GlStateManager.disableTexture2D();

        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer buffer = tessellator.getBuffer();

        GL11.glEnable(GL11.GL_SCISSOR_TEST);

        renderFaces(tessellator, buffer, stack);
        BlockPos dimension = ShapeCardItem.getDimension(stack);
        renderHelpers(tessellator, buffer, dimension.getX(), dimension.getY(), dimension.getZ(), showAxis, showOuter);

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        GlStateManager.popMatrix();

        if (showGuidelines) {
            GlStateManager.glLineWidth(3);
            buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(x - 62, y + 180, 0).color(1f, 0f, 0f, 1f).endVertex();
            buffer.pos(x - 39, y + 180, 0).color(1f, 0f, 0f, 1f).endVertex();
            buffer.pos(x - 62, y + 195, 0).color(0f, 0.8f, 0f, 1f).endVertex();
            buffer.pos(x - 39, y + 195, 0).color(0f, 0.8f, 0f, 1f).endVertex();
            buffer.pos(x - 62, y + 210, 0).color(0f, 0f, 1f, 1f).endVertex();
            buffer.pos(x - 39, y + 210, 0).color(0f, 0f, 1f, 1f).endVertex();
            tessellator.draw();
        }

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        RenderHelper.enableGUIStandardItemLighting();

        RenderData data = ShapeDataManager.getRenderData(shapeID);
        if (data != null && !data.previewMessage.isEmpty()) {
            Minecraft.getMinecraft().fontRenderer.drawString(data.previewMessage, gui.getPreviewLeft()+84, gui.getPreviewTop()+50, 0xffff0000);
        }

    }

    private void renderHelpers(Tessellator tessellator, VertexBuffer buffer, int xlen, int ylen, int zlen, boolean showAxis, boolean showOuter) {
        // X, Y, Z axis
        if (showAxis) {
            ShapeRenderer.renderAxis(tessellator, buffer, xlen/2, ylen/2, zlen/2);
        }

        if (showOuter) {
            ShapeRenderer.renderOuterBox(tessellator, buffer, xlen, ylen, zlen);
        }
    }



    static void renderOuterBox(Tessellator tessellator, VertexBuffer buffer, int xlen, int ylen, int zlen) {
        GlStateManager.glLineWidth(1.0f);
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
//        buffer.setTranslation(0.5, 0.5, 0.5);
        int xleft = -xlen / 2;
        int xright = xlen / 2 + (xlen & 1);
        int ybot = -ylen / 2;
        int ytop = ylen / 2 + (ylen & 1);
        int zsouth = -zlen / 2;
        int znorth = zlen / 2 + (zlen & 1);

        buffer.pos(xleft, ybot, zsouth).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(xright, ybot, zsouth).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(xleft, ybot, zsouth).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(xleft, ytop, zsouth).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(xleft, ybot, zsouth).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(xleft, ybot, znorth).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(xright, ytop, znorth).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(xleft, ytop, znorth).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(xright, ytop, znorth).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(xright, ybot, znorth).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(xright, ytop, znorth).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(xright, ytop, zsouth).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(xright, ybot, zsouth).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(xright, ybot, znorth).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(xright, ybot, zsouth).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(xright, ytop, zsouth).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(xleft, ytop, zsouth).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(xright, ytop, zsouth).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(xleft, ytop, zsouth).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(xleft, ytop, znorth).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(xleft, ytop, znorth).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(xleft, ybot, znorth).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(xleft, ybot, znorth).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(xright, ybot, znorth).color(1f, 1f, 1f, 1f).endVertex();
        buffer.setTranslation(0, 0, 0);
        tessellator.draw();
    }

    static void renderAxis(Tessellator tessellator, VertexBuffer buffer, int xlen, int ylen, int zlen) {
        GlStateManager.glLineWidth(2.5f);
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
//        buffer.setTranslation(0.5, 0.5, 0.5);
        buffer.pos(0, 0, 0).color(1f, 0f, 0f, 1f).endVertex();
        buffer.pos(xlen, 0, 0).color(1f, 0f, 0f, 1f).endVertex();
        buffer.pos(0, 0, 0).color(0f, 1f, 0f, 1f).endVertex();
        buffer.pos(0, ylen, 0).color(0f, 1f, 0f, 1f).endVertex();
        buffer.pos(0, 0, 0).color(0f, 0f, 1f, 1f).endVertex();
        buffer.pos(0, 0, zlen).color(0f, 0f, 1f, 1f).endVertex();
        buffer.setTranslation(0, 0, 0);
        tessellator.draw();
    }

    private int calculateChecksum(ItemStack stack) {
        Check32 crc = new Check32();
        if (ItemStackTools.isValid(stack)) {
            ShapeCardItem.getFormulaCheckClient(stack, crc);
        }
        return crc.get();
    }

    private static class Col {
        private final float r;
        private final float g;
        private final float b;

        public Col(float r, float g, float b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }

        public float getR() {
            return r;
        }

        public float getG() {
            return g;
        }

        public float getB() {
            return b;
        }
    }

    private static final Col COL_DEFAULT = new Col(.5f,.3f,.5f);
    private static final Col COL_LAVA = new Col(0xd4/255.0f,0x5a/255.0f,0x12/255.0f);
    private static final Col COL_NETHERBRICK = new Col(0x2d/255.0f,0x17/255.0f,0x1b/255.0f);
    private static final Col COL_SCANNER = new Col(0x00/255.0f,0x00/255.0f,0xe2/255.0f);

    public static class BlockDim {
        private final float height;
        private final float offset;

        public BlockDim(float offset, float height) {
            this.height = height;
            this.offset = offset;
        }

        public float getHeight() {
            return height;
        }

        public float getOffset() {
            return offset;
        }
    }

    private static final BlockDim BD_RAIL = new BlockDim(.1f, .2f);
    private static final BlockDim BD_GRASS = new BlockDim(.1f, .2f);
    private static final BlockDim BD_TORCH = new BlockDim(.4f, .7f);
    private static final BlockDim BD_FLOWER = new BlockDim(.4f, .6f);
    private static final BlockDim BD_MUSHROOM = new BlockDim(.3f, .5f);
    private static final BlockDim BD_BARS = new BlockDim(.4f, 1);
    private static final BlockDim BD_VINE = new BlockDim(.4f, 1);
    private static final BlockDim BD_WALL = new BlockDim(.25f, .9f);
    private static final BlockDim BD_SLAB = new BlockDim(.05f, .4f);
    private static final BlockDim BD_FIRE = new BlockDim(.1f, .3f);

    public static BlockDim getBlockDim(Map<IBlockState, BlockDim> palette, IBlockState state) {
        if (state == null) {
            return null;
        }
        if (palette.containsKey(state)) {
            return palette.get(state);
        }
        BlockDim bd = null;
        Block block = state.getBlock();
        if (block == Blocks.TORCH || block == Blocks.REDSTONE_TORCH) {
            bd = BD_TORCH;
        } else if (block == Blocks.SNOW_LAYER || block == Blocks.STONE_SLAB || block == Blocks.WOODEN_SLAB) {
            bd = BD_SLAB;
        } else if (block == Blocks.COBBLESTONE_WALL) {
            bd = BD_WALL;
        } else if (block == Blocks.IRON_BARS || block == Blocks.LADDER) {
            bd = BD_BARS;
        } else if (block == Blocks.VINE) {
            bd = BD_VINE;
        } else if (block == Blocks.RED_FLOWER || block == Blocks.YELLOW_FLOWER) {
            bd = BD_FLOWER;
        } else if (block == Blocks.TALLGRASS) {
            bd = BD_GRASS;
        } else if (block == Blocks.RAIL || block == Blocks.ACTIVATOR_RAIL || block == Blocks.DETECTOR_RAIL || block == Blocks.GOLDEN_RAIL) {
            bd = BD_RAIL;
        } else if (block == Blocks.RED_MUSHROOM || block == Blocks.BROWN_MUSHROOM) {
            bd = BD_MUSHROOM;
        } else if (block == Blocks.FIRE) {
            bd = BD_FIRE;
        }
        palette.put(state, bd);
        return bd;
    }

    private Col getColor(Map<IBlockState, Col> palette, IBlockState state) {
        if (state == null) {
            return COL_DEFAULT;
        }
        if (palette.containsKey(state)) {
            return palette.get(state);
        }
        Col col;
        Block block = state.getBlock();
        MapColor mapColor = block.getMapColor(state);
        if (block == Blocks.LAVA || block == Blocks.FLOWING_LAVA) {
            col = COL_LAVA;
        } else if (block == Blocks.NETHER_BRICK || block == Blocks.NETHER_BRICK_FENCE || block == Blocks.NETHER_BRICK_STAIRS) {
            col = COL_NETHERBRICK;
        } else if (block == BuilderSetup.scannerBlock) {
            col = COL_SCANNER;
        } else if (mapColor != null) {
            col = new Col(((mapColor.colorValue>>16) & 0xff) / 255.0f, ((mapColor.colorValue>>8) & 0xff) / 255.0f, (mapColor.colorValue & 0xff) / 255.0f);
        } else {
            col = COL_DEFAULT;
        }
        float r = col.getR();
        float g = col.getG();
        float b = col.getB();
        if (r * 1.2f > 1.0f) {
            r = 0.99f/1.2f;
        }
        if (g * 1.2f > 1.0f) {
            g = 0.99f/1.2f;
        }
        if (b * 1.2f > 1.0f) {
            b = 0.99f/1.2f;
        }
        col = new Col(r, g, b);
        palette.put(state, col);
        return col;
    }


    private void renderFaces(Tessellator tessellator, final VertexBuffer buffer,
                     ItemStack stack) {

        RenderData data = getRenderDataAndCreate(shapeID);

        if (data.isWantData() || waitForNewRequest > 0) {
            if (waitForNewRequest <= 0) {
                // No positions, send a new request
                RFToolsMessages.INSTANCE.sendToServer(new PacketRequestShapeData(stack, shapeID));
                waitForNewRequest = 20;
                data.setWantData(false);
            } else {
                waitForNewRequest--;
            }
        } else {
            long check = calculateChecksum(stack);
            if (!data.hasData() || check != data.getChecksum()) {
                // Checksum failed, we want new data
                data.setChecksum(check);
                data.setWantData(true);
            }
        }

        if (data.getPlanes() != null) {
            long time = System.currentTimeMillis();
            for (RenderData.RenderPlane plane : data.getPlanes()) {
                if (plane != null) {
                    if (plane.isDirty()) {
                        createRenderData(tessellator, buffer, plane, data);
                        plane.markClean();
                    }
                    boolean flash = plane.getBirthtime() > time-200;
                    if (flash) {
                        GlStateManager.enableBlend();
                        GlStateManager.blendFunc(GL11.GL_ONE, GL11.GL_ONE);
//                        GlStateManager.colorMask(false, false, true, true);
                    }
                    plane.render();
                    if (flash) {
                        GlStateManager.disableBlend();
                        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//                        GlStateManager.colorMask(true, true, true, true);
                    }
                }
            }
        }
    }

    private void createRenderData(Tessellator tessellator, VertexBuffer buffer, RenderData.RenderPlane plane, RenderData data) {
        Map<IBlockState, Col> pallete = new HashMap<>();
        Map<IBlockState, BlockDim> bdpal = new HashMap<>();

        double origOffsetX = buffer.xOffset;
        double origOffsetY = buffer.yOffset;
        double origOffsetZ = buffer.zOffset;

        int avgcnt = 0;
        int total = 0;
        int y = plane.getY();
        int offsety = plane.getOffsety();

        buffer = data.createRenderList(buffer, offsety);
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        for (RenderData.RenderStrip strip : plane.getStrips()) {
            int z = plane.getStartz();
            int x = strip.getX();
            List<Pair<Integer, IBlockState>> columnData = strip.getData();
            for (int i = 0; i < columnData.size(); i++) {
                Pair<Integer, IBlockState> pair = columnData.get(i);
                int cnt = pair.getKey();
                IBlockState state = pair.getValue();
                if (state != null) {
                    buffer.setTranslation(origOffsetX + x, origOffsetY + y, origOffsetZ + z);
                    avgcnt += cnt;
                    total++;
                    Col col = getColor(pallete, state);
                    float r = col.getR();
                    float g = col.getG();
                    float b = col.getB();
                    BlockDim bd = getBlockDim(bdpal, state);
                    if (bd == null) {
                        addSideFullTextureUp(buffer, cnt, r * .8f, g * .8f, b * .8f);
                        addSideFullTextureDown(buffer, cnt, r * .8f, g * .8f, b * .8f);
                        if (strip.isEmptyAt(i - 1, bdpal)) {
                            addSideFullTextureNorth(buffer, cnt, r * 1.2f, g * 1.2f, b * 1.2f);
                        }
                        if (strip.isEmptyAt(i + 1, bdpal)) {
                            addSideFullTextureSouth(buffer, cnt, r * 1.2f, g * 1.2f, b * 1.2f);
                        }
                        addSideFullTextureWest(buffer, cnt, r, g, b);
                        addSideFullTextureEast(buffer, cnt, r, g, b);
                    } else {
                        for (int c = 0 ; c < cnt ; c++) {
                            addSideFullTextureUp(buffer, bd.getOffset(), bd.getHeight(), c, r * .8f, g * .8f, b * .8f);
                            addSideFullTextureDown(buffer, bd.getOffset(), bd.getHeight(), c, r * .8f, g * .8f, b * .8f);
                            addSideFullTextureNorth(buffer, bd.getOffset(), bd.getHeight(), c, r * 1.2f, g * 1.2f, b * 1.2f);
                            addSideFullTextureSouth(buffer, bd.getOffset(), bd.getHeight(), c, r * 1.2f, g * 1.2f, b * 1.2f);
                            addSideFullTextureWest(buffer, bd.getOffset(), bd.getHeight(), c, r, g, b);
                            addSideFullTextureEast(buffer, bd.getOffset(), bd.getHeight(), c, r, g, b);
                        }
                    }
                }
                z += cnt;
            }
        }

        buffer.setTranslation(origOffsetX, origOffsetY, origOffsetZ);
        data.performRenderToList(tessellator, buffer, offsety);

//        float avg = avgcnt / (float) total;
//        System.out.println("y = " + offsety + ", avg = " + avg + ", quads = " + quadcnt);
    }


    private static void setupScissor(IShapeParentGui gui) {
        Minecraft mc = Minecraft.getMinecraft();

        final ScaledResolution scaledresolution = new ScaledResolution(mc);
        int xScale = scaledresolution.getScaledWidth();
        int yScale = scaledresolution.getScaledHeight();
        int sx = (gui.getPreviewLeft() + 84) * mc.displayWidth / xScale;
        int sy = (mc.displayHeight) - (gui.getPreviewTop() + 136) * mc.displayHeight / yScale;
        int sw = 161 * mc.displayWidth / xScale;
        int sh = 130 * mc.displayHeight / yScale;

        GL11.glScissor(sx, sy, sw, sh);
    }

    private static void addSideFullTextureDown(VertexBuffer buffer, int cnt, float r, float g, float b) {
        float a = 0.5f;
        buffer.pos(0, 0, 0).color(r, g, b, a).endVertex();
        buffer.pos(1, 0, 0).color(r, g, b, a).endVertex();
        buffer.pos(1, 0, cnt).color(r, g, b, a).endVertex();
        buffer.pos(0, 0, cnt).color(r, g, b, a).endVertex();
    }

    private static void addSideFullTextureUp(VertexBuffer buffer, int cnt, float r, float g, float b) {
        float a = 0.5f;
        buffer.pos(0, 1, cnt).color(r, g, b, a).endVertex();
        buffer.pos(1, 1, cnt).color(r, g, b, a).endVertex();
        buffer.pos(1, 1, 0).color(r, g, b, a).endVertex();
        buffer.pos(0, 1, 0).color(r, g, b, a).endVertex();
    }

    private static void addSideFullTextureEast(VertexBuffer buffer, int cnt, float r, float g, float b) {
        float a = 0.5f;
        buffer.pos(1, 0, 0).color(r, g, b, a).endVertex();
        buffer.pos(1, 1, 0).color(r, g, b, a).endVertex();
        buffer.pos(1, 1, cnt).color(r, g, b, a).endVertex();
        buffer.pos(1, 0, cnt).color(r, g, b, a).endVertex();
    }

    private static void addSideFullTextureWest(VertexBuffer buffer, int cnt, float r, float g, float b) {
        float a = 0.5f;
        buffer.pos(0, 0, cnt).color(r, g, b, a).endVertex();
        buffer.pos(0, 1, cnt).color(r, g, b, a).endVertex();
        buffer.pos(0, 1, 0).color(r, g, b, a).endVertex();
        buffer.pos(0, 0, 0).color(r, g, b, a).endVertex();
    }

    private static void addSideFullTextureNorth(VertexBuffer buffer, int cnt, float r, float g, float b) {
        float a = 0.5f;
        buffer.pos(1, 1, 0).color(r, g, b, a).endVertex();
        buffer.pos(1, 0, 0).color(r, g, b, a).endVertex();
        buffer.pos(0, 0, 0).color(r, g, b, a).endVertex();
        buffer.pos(0, 1, 0).color(r, g, b, a).endVertex();
    }

    private static void addSideFullTextureSouth(VertexBuffer buffer, int cnt, float r, float g, float b) {
        float a = 0.5f;
        buffer.pos(1, 0, cnt).color(r, g, b, a).endVertex();
        buffer.pos(1, 1, cnt).color(r, g, b, a).endVertex();
        buffer.pos(0, 1, cnt).color(r, g, b, a).endVertex();
        buffer.pos(0, 0, cnt).color(r, g, b, a).endVertex();
    }

    private static void addSideFullTextureDown(VertexBuffer buffer, float offset, float height, int c, float r, float g, float b) {
        float a = 0.5f;
        buffer.pos(offset, 0, offset+c).color(r, g, b, a).endVertex();
        buffer.pos(1-offset, 0, offset+c).color(r, g, b, a).endVertex();
        buffer.pos(1-offset, 0, 1-offset+c).color(r, g, b, a).endVertex();
        buffer.pos(offset, 0, 1-offset+c).color(r, g, b, a).endVertex();
    }

    private static void addSideFullTextureUp(VertexBuffer buffer, float offset, float height, int c, float r, float g, float b) {
        float a = 0.5f;
        buffer.pos(offset, height, 1-offset+c).color(r, g, b, a).endVertex();
        buffer.pos(1-offset, height, 1-offset+c).color(r, g, b, a).endVertex();
        buffer.pos(1-offset, height, offset+c).color(r, g, b, a).endVertex();
        buffer.pos(offset, height, offset+c).color(r, g, b, a).endVertex();
    }

    private static void addSideFullTextureEast(VertexBuffer buffer, float offset, float height, int c, float r, float g, float b) {
        float a = 0.5f;
        buffer.pos(1-offset, 0, offset+c).color(r, g, b, a).endVertex();
        buffer.pos(1-offset, height, offset+c).color(r, g, b, a).endVertex();
        buffer.pos(1-offset, height, 1-offset+c).color(r, g, b, a).endVertex();
        buffer.pos(1-offset, 0, 1-offset+c).color(r, g, b, a).endVertex();
    }

    private static void addSideFullTextureWest(VertexBuffer buffer, float offset, float height, int c, float r, float g, float b) {
        float a = 0.5f;
        buffer.pos(offset, 0, 1-offset+c).color(r, g, b, a).endVertex();
        buffer.pos(offset, height, 1-offset+c).color(r, g, b, a).endVertex();
        buffer.pos(offset, height, offset+c).color(r, g, b, a).endVertex();
        buffer.pos(offset, 0, offset+c).color(r, g, b, a).endVertex();
    }

    private static void addSideFullTextureNorth(VertexBuffer buffer, float offset, float height, int c, float r, float g, float b) {
        float a = 0.5f;
        buffer.pos(1-offset, height, offset+c).color(r, g, b, a).endVertex();
        buffer.pos(1-offset, 0, offset+c).color(r, g, b, a).endVertex();
        buffer.pos(offset, 0, offset+c).color(r, g, b, a).endVertex();
        buffer.pos(offset, height, offset+c).color(r, g, b, a).endVertex();
    }

    private static void addSideFullTextureSouth(VertexBuffer buffer, float offset, float height, int c, float r, float g, float b) {
        float a = 0.5f;
        buffer.pos(1-offset, 0, 1-offset+c).color(r, g, b, a).endVertex();
        buffer.pos(1-offset, height, 1-offset+c).color(r, g, b, a).endVertex();
        buffer.pos(offset, height, 1-offset+c).color(r, g, b, a).endVertex();
        buffer.pos(offset, 0, 1-offset+c).color(r, g, b, a).endVertex();
    }

}
