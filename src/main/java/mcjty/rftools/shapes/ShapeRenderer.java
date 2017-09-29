package mcjty.rftools.shapes;

import mcjty.rftools.blocks.builder.BuilderSetup;
import mcjty.rftools.items.builder.ShapeCardItem;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
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

    private final String name;    // For debug
    private final int id;         // Unique ID for this renderer
    private static int lastId = 0;
    private int glList = -1;
    private long checksum = -1;
    private boolean prevShowMat = false;

    private int waitForNewRequest = 0;


    public static class RenderData {
        private Map<Long, IBlockState> positions = null;
        public int shapeCount = 0;
        public String previewMessage = "";
    }

    private static final Map<Integer, RenderData> renderDataMap = new HashMap<>();

    public ShapeRenderer(String name) {
        this.name = name;
        this.id = lastId++;
    }

    public int getId() {
        return id;
    }

    public int getCount() {
        if (renderDataMap.containsKey(id)) {
            return renderDataMap.get(id).shapeCount;
        }
        return 0;
    }

    public static void setRenderData(int id, Map<Long, IBlockState> positions, int count, String msg) {
        RenderData data = new RenderData();
        data.positions = positions;//new HashMap<>(positions);
        data.shapeCount = count;
        data.previewMessage = msg;
        renderDataMap.put(id, data);
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
                    System.out.println("dx = " + dx + "," + dy);
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

    public void invalidateGlList() {
        if (glList != -1) {
            GLAllocation.deleteDisplayLists(glList);
        }
        glList = -1;
    }

    public void renderShapeInWorld(ItemStack stack, double x, double y, double z) {
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x + 0.5F, (float) y + 3F, (float) z + 0.5F);
        GlStateManager.scale(0.02,0.02,0.02);

        net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
        Minecraft.getMinecraft().entityRenderer.disableLightmap();
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.disableLighting();
        GlStateManager.disableTexture2D();

        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer buffer = tessellator.getBuffer();
        renderFaces(tessellator, buffer, stack, true);

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        RenderHelper.enableStandardItemLighting();
        Minecraft.getMinecraft().entityRenderer.enableLightmap();

        GlStateManager.popMatrix();
    }

    public void renderShape(IShapeParentGui gui, ItemStack stack, int x, int y, boolean showAxis, boolean showOuter, boolean showMat) {
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

        renderFaces(tessellator, buffer, stack, showMat);
        BlockPos dimension = ShapeCardItem.getDimension(stack);
        renderHelpers(tessellator, buffer, dimension.getX(), dimension.getY(), dimension.getZ(), showAxis, showOuter);

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        GlStateManager.popMatrix();

        GlStateManager.glLineWidth(3);
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(x-62, y+180, 0)  .color(1f, 0f, 0f, 1f).endVertex();
        buffer.pos(x-39, y+180, 0)  .color(1f, 0f, 0f, 1f).endVertex();
        buffer.pos(x-62, y+195, 0)  .color(0f, 0.8f, 0f, 1f).endVertex();
        buffer.pos(x-39, y+195, 0)  .color(0f, 0.8f, 0f, 1f).endVertex();
        buffer.pos(x-62, y+210, 0)  .color(0f, 0f, 1f, 1f).endVertex();
        buffer.pos(x-39, y+210, 0)  .color(0f, 0f, 1f, 1f).endVertex();
        tessellator.draw();


        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        RenderHelper.enableGUIStandardItemLighting();

        RenderData data = renderDataMap.get(id);
        if (data != null && !data.previewMessage.isEmpty()) {
            Minecraft.getMinecraft().fontRenderer.drawString(data.previewMessage, gui.getPreviewLeft()+84, gui.getPreviewTop()+50, 0xffff0000);
            return;
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



    private static final Quad[] QUADS = new Quad[] {
            new Quad(new Vt(0, 0, 0), new Vt(1, 0, 0), new Vt(1, 0, 1), new Vt(0, 0, 1)),       // DOWN
            new Quad(new Vt(0, 1, 1), new Vt(1, 1, 1), new Vt(1, 1, 0), new Vt(0, 1, 0)),       // UP
            new Quad(new Vt(1, 1, 0), new Vt(1, 0, 0), new Vt(0, 0, 0), new Vt(0, 1, 0)),       // NORTH
            new Quad(new Vt(1, 0, 1), new Vt(1, 1, 1), new Vt(0, 1, 1), new Vt(0, 0, 1)),       // SOUTH
            new Quad(new Vt(0, 0, 1), new Vt(0, 1, 1), new Vt(0, 1, 0), new Vt(0, 0, 0)),       // WEST
            new Quad(new Vt(1, 0, 0), new Vt(1, 1, 0), new Vt(1, 1, 1), new Vt(1, 0, 1)),       // EAST
    };

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

    private long calculateChecksum(ItemStack stack) {
        return ShapeCardItem.getCheck(stack);
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
    private static final Col COL_SCANNER = new Col(0x00/255.0f,0x00/255.0f,0xe2/255.0f);

    private Col getColor(Map<IBlockState, Col> pallete, IBlockState state) {
        if (state == null) {
            return COL_DEFAULT;
        }
        if (pallete.containsKey(state)) {
            return pallete.get(state);
        }
        Col col;
        Block block = state.getBlock();
        MapColor mapColor = block.getMapColor(state);
        if (block == Blocks.LAVA || block == Blocks.FLOWING_LAVA) {
            col = COL_LAVA;
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
        pallete.put(state, col);
        return col;
    }


//    private void renderFacesVBO(Tessellator tessellator, final VertexBuffer buffer,
//                                ItemStack stack, boolean showMat) {
//        if (vboBuffer == null) {
//            vboBuffer = new VertexBuffer(2097152);
//        }
//        buffer
//    }
//
    private void renderFaces(Tessellator tessellator, final VertexBuffer buffer,
                     ItemStack stack, boolean showMat) {

        RenderData data = renderDataMap.get(id);

        if (data == null || data.positions == null || waitForNewRequest > 0) {
            if (waitForNewRequest <= 0) {
                // No positions, send a new request
                RFToolsMessages.INSTANCE.sendToServer(new PacketRequestShapeData(stack, id));
                waitForNewRequest = 10;
                if (data != null) {
                    data.positions = null;
                }
            } else {
                waitForNewRequest--;
                if (data != null && data.positions != null) {
                    // Positions have arrived, create displayList
                    // Data is received
                    waitForNewRequest = 0;
                    checksum = calculateChecksum(stack);
                    createDisplayList(tessellator, buffer, showMat);
                }
            }
            if (glList != -1) {
                // Render old data while we're waiting
                GlStateManager.callList(glList);
            }
            return;
        }

        long check = calculateChecksum(stack);
        if (glList == -1 || check != checksum || showMat != prevShowMat) {
            // Checksum failed, set positions to null
            data.positions = null;
        }

        if (glList != -1) {
            GlStateManager.callList(glList);
        }
    }

    private void createDisplayList(Tessellator tessellator, VertexBuffer buffer, boolean showMat) {
        prevShowMat = showMat;
        invalidateGlList();
        glList = GLAllocation.generateDisplayLists(1);
        GlStateManager.glNewList(glList, GL11.GL_COMPILE);

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
//        GlStateManager.enableBlend();
//        GlStateManager.enableAlpha();

//            Map<Long, IBlockState> stateMap = new HashMap<>();
//            TLongHashSet positions = ShapeCardItem.getPositions(stack, shape, false, new BlockPos(0, 0, 0), new BlockPos(0, 0, 0), stateMap);
        Map<IBlockState, Col> pallete = new HashMap<>();

        RenderData data = renderDataMap.get(id);
        Map<Long, IBlockState> positions = data.positions;

        for (Map.Entry<Long, IBlockState> entry : positions.entrySet()) {
            long p = entry.getKey();
            BlockPos coordinate = BlockPos.fromLong(p);
            int x = coordinate.getX();
            int y = coordinate.getY();
            int z = coordinate.getZ();

            buffer.setTranslation(buffer.xOffset + x, buffer.yOffset + y, buffer.zOffset + z);
            if (showMat) {
                Col col = getColor(pallete, entry.getValue());
                float r = col.getR();
                float g = col.getG();
                float b = col.getB();
                if (!positions.containsKey(coordinate.up().toLong())) {
                    addSideFullTexture(buffer, EnumFacing.UP.ordinal(), r * .8f, g * .8f, b * .8f);
                }
                if (!positions.containsKey(coordinate.down().toLong())) {
                    addSideFullTexture(buffer, EnumFacing.DOWN.ordinal(), r * .8f, g * .8f, b * .8f);
                }
                if (!positions.containsKey(coordinate.north().toLong())) {
                    addSideFullTexture(buffer, EnumFacing.NORTH.ordinal(), r * 1.2f, g * 1.2f, b * 1.2f);
                }
                if (!positions.containsKey(coordinate.south().toLong())) {
                    addSideFullTexture(buffer, EnumFacing.SOUTH.ordinal(), r * 1.2f, g * 1.2f, b * 1.2f);
                }
                if (!positions.containsKey(coordinate.west().toLong())) {
                    addSideFullTexture(buffer, EnumFacing.WEST.ordinal(), r, g, b);
                }
                if (!positions.containsKey(coordinate.east().toLong())) {
                    addSideFullTexture(buffer, EnumFacing.EAST.ordinal(), r, g, b);
                }
            } else {
                float d = .2f;
                float l = ((x + y + z) & 1) == 1 ? .9f : .6f;
                if (!positions.containsKey(coordinate.up().toLong())) {
                    addSideFullTexture(buffer, EnumFacing.UP.ordinal(), d, l, d);
                }
                if (!positions.containsKey(coordinate.down().toLong())) {
                    addSideFullTexture(buffer, EnumFacing.DOWN.ordinal(), d, l, d);
                }
                if (!positions.containsKey(coordinate.north().toLong())) {
                    addSideFullTexture(buffer, EnumFacing.NORTH.ordinal(), d, d, l);
                }
                if (!positions.containsKey(coordinate.south().toLong())) {
                    addSideFullTexture(buffer, EnumFacing.SOUTH.ordinal(), d, d, l);
                }
                if (!positions.containsKey(coordinate.west().toLong())) {
                    addSideFullTexture(buffer, EnumFacing.WEST.ordinal(), l, d, d);
                }
                if (!positions.containsKey(coordinate.east().toLong())) {
                    addSideFullTexture(buffer, EnumFacing.EAST.ordinal(), l, d, d);
                }
            }
            buffer.setTranslation(buffer.xOffset - x, buffer.yOffset - y, buffer.zOffset - z);
        }
        tessellator.draw();
        GlStateManager.glEndList();
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


    public static void addSideFullTexture(VertexBuffer buffer, int side, float r, float g, float b) {
        Quad quad = QUADS[side];
        float a = 0.5f;
        buffer.pos(quad.v1.x, quad.v1.y, quad.v1.z).color(r, g, b, a).endVertex();
        buffer.pos(quad.v2.x, quad.v2.y, quad.v2.z).color(r, g, b, a).endVertex();
        buffer.pos(quad.v3.x, quad.v3.y, quad.v3.z).color(r, g, b, a).endVertex();
        buffer.pos(quad.v4.x, quad.v4.y, quad.v4.z).color(r, g, b, a).endVertex();
    }

    private static class Vt {
        public final float x;
        public final float y;
        public final float z;

        public Vt(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    private static class Quad {
        public final Vt v1;
        public final Vt v2;
        public final Vt v3;
        public final Vt v4;

        public Quad(Vt v1, Vt v2, Vt v3, Vt v4) {
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
            this.v4 = v4;
        }
    }
}
