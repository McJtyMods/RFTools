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
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.*;

import static mcjty.rftools.blocks.builder.BuilderConfiguration.useVBO;

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
    private long checksum = -1;
    private boolean prevShowMat = false;

    private int waitForNewRequest = 0;


    public static class RenderData {
        private ShapeRenderer.RenderColumn columns[] = null;
        public int shapeCount = 0;
        public String previewMessage = "";
        private int glList = -1;
        private net.minecraft.client.renderer.vertex.VertexBuffer vbo;
        private long touchTime = 0;

        public boolean hasData() {
            if (useVBO) {
                return vbo != null;
            } else {
                return glList != -1;
            }
        }
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

    public static RenderData getRenderData(int id) {
        RenderData data = renderDataMap.get(id);
        if (data == null) {
            data = new RenderData();
            renderDataMap.put(id, data);
        }
        data.touchTime = System.currentTimeMillis();
        return data;
    }

    private static int cleanupCounter = 20;
    public static void cleanupOldRenderers() {
        cleanupCounter--;
        if (cleanupCounter >= 0) {
            return;
        }
        cleanupCounter = 20;
        long current = System.currentTimeMillis();
        Set<Integer> toRemove = new HashSet<>();
        for (Map.Entry<Integer, RenderData> entry : renderDataMap.entrySet()) {
            if (entry.getValue().touchTime + 5000 < current) {
                System.out.println("Removing id = " + entry.getKey());
                toRemove.add(entry.getKey());
            }
        }
        for (Integer id : toRemove) {
            RenderData data = renderDataMap.get(id);
            cleanupGLBuffer(data);
            renderDataMap.remove(id);
        }
    }

    private static void cleanupGLBuffer(RenderData data) {
        if (useVBO) {
            if (data.vbo != null) {
                data.vbo.deleteGlBuffers();
                data.vbo = null;
            }
        } else {
            if (data.glList != -1) {
                GLAllocation.deleteDisplayLists(data.glList);
                data.glList = -1;
            }
        }
    }

    public static void setRenderData(int id, ShapeRenderer.RenderColumn columns[], int count, String msg) {
        RenderData data = getRenderData(id);
        data.columns = columns;
        data.shapeCount = count;
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

    public void invalidateGlList() {
        RenderData data = renderDataMap.get(id);
        if (data == null) {
            return;
        }
        cleanupGLBuffer(data);
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

        RenderData data = getRenderData(id);

        if (data.columns == null || waitForNewRequest > 0) {
            if (waitForNewRequest <= 0) {
                // No positions, send a new request
                RFToolsMessages.INSTANCE.sendToServer(new PacketRequestShapeData(stack, id));
                waitForNewRequest = 10;
                data.columns = null;
            } else {
                waitForNewRequest--;
                if (data.columns != null) {
                    // Positions have arrived, create displayList
                    // Data is received
                    waitForNewRequest = 0;
                    checksum = calculateChecksum(stack);
                    createDisplayList(tessellator, buffer, showMat);
                }
            }
            if (data.hasData()) {
                // Render old data while we're waiting
                renderData(data);
            }
            return;
        }

        long check = calculateChecksum(stack);
        if (!data.hasData() || check != checksum || showMat != prevShowMat) {
            // Checksum failed, set positions to null
            data.columns = null;
        }

        if (data.hasData()) {
            renderData(data);
        }
    }

    private void renderData(RenderData data) {
        if (useVBO) {
            //...
            data.vbo.bindBuffer();
            GlStateManager.glEnableClientState(GL11.GL_VERTEX_ARRAY);
            GlStateManager.glVertexPointer(3, GL11.GL_FLOAT, 16, 0);
            GlStateManager.glEnableClientState(GL11.GL_COLOR_ARRAY);
            GlStateManager.glColorPointer(4, GL11.GL_UNSIGNED_BYTE, 16, 12);
            data.vbo.drawArrays(7);
            data.vbo.unbindBuffer();
            GlStateManager.glDisableClientState(GL11.GL_COLOR_ARRAY);
            GlStateManager.glDisableClientState(GL11.GL_VERTEX_ARRAY);
        } else {
            GlStateManager.callList(data.glList);
        }
    }


    private static VertexBuffer vboBuffer = new VertexBuffer(2097152);

    private void createDisplayList(Tessellator tessellator, VertexBuffer buffer, boolean showMat) {
        prevShowMat = showMat;
        invalidateGlList();
        RenderData data = getRenderData(id);

        if (useVBO) {
            data.vbo = new net.minecraft.client.renderer.vertex.VertexBuffer(DefaultVertexFormats.POSITION_COLOR);
            buffer = vboBuffer;
        } else {
            data.glList = GLAllocation.generateDisplayLists(1);
            GlStateManager.glNewList(data.glList, GL11.GL_COMPILE);
        }

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
//        GlStateManager.enableBlend();
//        GlStateManager.enableAlpha();

//            Map<Long, IBlockState> stateMap = new HashMap<>();
        Map<IBlockState, Col> pallete = new HashMap<>();

        double origOffsetX = buffer.xOffset;
        double origOffsetY = buffer.yOffset;
        double origOffsetZ = buffer.zOffset;

        int avgcnt = 0;
        int total = 0;
        RenderColumn[] columns = data.columns;
        for (RenderColumn column : columns) {
            BlockPos coordinate = column.getBottomPos();
            int x = coordinate.getX();
            int y = coordinate.getY();
            int z = coordinate.getZ();
            List<Pair<Integer, IBlockState>> columnData = column.getData();
            for (int i = 0 ; i < columnData.size() ; i++) {
                Pair<Integer, IBlockState> pair = columnData.get(i);
                int cnt = pair.getKey();
                IBlockState state = pair.getValue();
                if (state != null) {
                    buffer.setTranslation(origOffsetX + x, origOffsetY + y, origOffsetZ + z);
                    avgcnt += cnt;
                    total++;
                    if (showMat) {
                        Col col = getColor(pallete, state);
                        float r = col.getR();
                        float g = col.getG();
                        float b = col.getB();
                        if (column.isEmptyAt(i+1)) {
                            addSideFullTexture(buffer, EnumFacing.UP.ordinal(), cnt, r * .8f, g * .8f, b * .8f);
                        }
                        if (column.isEmptyAt(i-1)) {
                            addSideFullTexture(buffer, EnumFacing.DOWN.ordinal(), r * .8f, g * .8f, b * .8f);
                        }
                        addSideFullTexture(buffer, EnumFacing.NORTH.ordinal(), cnt, r * 1.2f, g * 1.2f, b * 1.2f);
                        addSideFullTexture(buffer, EnumFacing.SOUTH.ordinal(), cnt, r * 1.2f, g * 1.2f, b * 1.2f);
                        addSideFullTexture(buffer, EnumFacing.WEST.ordinal(), cnt, r, g, b);
                        addSideFullTexture(buffer, EnumFacing.EAST.ordinal(), cnt, r, g, b);
                    } else {
                        float d = .2f;
                        float l = ((x + y + z) & 1) == 1 ? .9f : .6f;
                        if (column.isEmptyAt(i+1)) {
                            addSideFullTexture(buffer, EnumFacing.UP.ordinal(), cnt, d, l, d);
                        }
                        if (column.isEmptyAt(i-1)) {
                            addSideFullTexture(buffer, EnumFacing.DOWN.ordinal(), d, l, d);
                        }
                        addSideFullTexture(buffer, EnumFacing.NORTH.ordinal(), cnt, d, d, l);
                        addSideFullTexture(buffer, EnumFacing.SOUTH.ordinal(), cnt, d, d, l);
                        addSideFullTexture(buffer, EnumFacing.WEST.ordinal(), cnt, l, d, d);
                        addSideFullTexture(buffer, EnumFacing.EAST.ordinal(), cnt, l, d, d);
                    }
                }
                y += cnt;
            }
        }
        float avg = avgcnt / (float) total;
        System.out.println("avg = " + avg);
        buffer.setTranslation(origOffsetX, origOffsetY, origOffsetZ);

        if (useVBO) {
            buffer.finishDrawing();
            buffer.reset();
            data.vbo.bufferData(buffer.getByteBuffer());

        } else {
            tessellator.draw();
            GlStateManager.glEndList();
        }
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

    public static void addSideFullTexture(VertexBuffer buffer, int side, int cnt, float r, float g, float b) {
        Quad quad = QUADS[side];
        float a = 0.5f;
        buffer.pos(quad.v1.x, quad.v1.y * cnt, quad.v1.z).color(r, g, b, a).endVertex();
        buffer.pos(quad.v2.x, quad.v2.y * cnt, quad.v2.z).color(r, g, b, a).endVertex();
        buffer.pos(quad.v3.x, quad.v3.y * cnt, quad.v3.z).color(r, g, b, a).endVertex();
        buffer.pos(quad.v4.x, quad.v4.y * cnt, quad.v4.z).color(r, g, b, a).endVertex();
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

    public static class RenderColumn {
        private final List<Pair<Integer, IBlockState>> data = new ArrayList<>();
        private final BlockPos bottomPos;
        private IBlockState last;
        private int cnt = 0;

        public RenderColumn(BlockPos bottomPos) {
            this.bottomPos = bottomPos;
        }

        public BlockPos getBottomPos() {
            return bottomPos;
        }

        public List<Pair<Integer, IBlockState>> getData() {
            return data;
        }

        public boolean isEmptyAt(int i) {
            if (i < 0) {
                return true;
            }
            if (i >= data.size()) {
                return true;
            }
            return data.get(i).getValue() == null;
        }

        public void add(IBlockState state) {
            if (cnt == 0) {
                last = state;
                cnt = 1;
            } else {
                if (last != state) {
                    data.add(Pair.of(cnt, last));
                    last = state;
                    cnt = 1;
                } else {
                    cnt++;
                }
            }
        }

        public void close() {
            if (cnt > 0) {
                data.add(Pair.of(cnt, last));
                cnt = 0;
            }
        }
    }
}
