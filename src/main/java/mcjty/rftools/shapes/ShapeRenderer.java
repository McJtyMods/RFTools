package mcjty.rftools.shapes;

import mcjty.lib.tools.ItemStackTools;
import mcjty.rftools.blocks.shaper.ScannerConfiguration;
import mcjty.rftools.items.builder.ShapeCardItem;
import mcjty.rftools.network.RFToolsMessages;
import mcjty.rftools.varia.Check32;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
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
        RenderData data = ShapeDataManagerClient.getRenderData(shapeID);
        if (data != null) {
            return data.getBlockCount();
        }
        return 0;
    }

    public static RenderData getRenderDataAndCreate(ShapeID shapeID) {
        RenderData data = ShapeDataManagerClient.getRenderDataAndCreate(shapeID);
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

    public boolean renderShapeInWorld(ItemStack stack, double x, double y, double z, float offset, float scale, float angle,
                                   boolean scan, ShapeID shape) {
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
        boolean doSound = renderFaces(tessellator, buffer, stack, scan, shape.isGrayscale(), shape.getScanId());

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        RenderHelper.enableStandardItemLighting();
        Minecraft.getMinecraft().entityRenderer.enableLightmap();

        GlStateManager.popMatrix();
        return doSound;
    }

    public void renderShape(IShapeParentGui gui, ItemStack stack, int x, int y, boolean showAxis, boolean showOuter, boolean showScan, boolean showGuidelines) {
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

        renderFaces(tessellator, buffer, stack, showScan, false, -1);
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

        RenderData data = ShapeDataManagerClient.getRenderData(shapeID);
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

    private int extraDataCounter = 0;

    private boolean renderFaces(Tessellator tessellator, final VertexBuffer buffer,
                     ItemStack stack, boolean showScan, boolean grayscale, int scanId) {

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

        boolean needScanSound = false;
        if (data.getPlanes() != null) {
            long time = System.currentTimeMillis();
            for (RenderData.RenderPlane plane : data.getPlanes()) {
                if (plane != null) {
                    if (plane.isDirty()) {
                        createRenderData(tessellator, buffer, plane, data, grayscale);
                        plane.markClean();
                    }
                    boolean flash = showScan && (plane.getBirthtime() > time-ScannerConfiguration.projectorFlashTimeout);
                    if (flash) {
                        needScanSound = true;
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

        // Possibly request extra data for the scan
        if (scanId != -1) {
            extraDataCounter--;
            if (extraDataCounter <= 0) {
                extraDataCounter = 10;
                ScanDataManagerClient.getScansClient().requestExtraDataClient(scanId);
            }
            ScanExtraData extraData = ScanDataManagerClient.getScansClient().getExtraDataClient(scanId);
            for (ScanExtraData.Beacon beacon : extraData.getBeacons()) {
                int x = beacon.getPos().getX();
                int y = beacon.getPos().getY()+1;
                int z = beacon.getPos().getZ();
                BeaconType type = beacon.getType();
                GlStateManager.translate(x, y, z);
                RenderData.RenderElement element = getBeaconElement(tessellator, buffer, type, beacon.isDoBeacon());
                element.render();
                GlStateManager.translate(-x, -y, -z);
            }
        }

        return needScanSound;
    }

    private void createRenderData(Tessellator tessellator, VertexBuffer buffer, RenderData.RenderPlane plane, RenderData data,
                                  boolean grayscale) {
        Map<IBlockState, ShapeBlockInfo> palette = new HashMap<>();

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
                    ShapeBlockInfo info = ShapeBlockInfo.getBlockInfo(palette, state);
                    ShapeBlockInfo.Col col = info.getCol();
                    float r = col.getR();
                    float g = col.getG();
                    float b = col.getB();
                    if (grayscale) {
//                        float a = (r+g+b)/3.0f;
                        float a = 0.21f*r+0.72f*g+0.07f*b;
                        r = g = b = a;
                    }
                    ShapeBlockInfo.IBlockRender bd = info.getRender();
                    if (bd == null) {
                        addSideFullTextureU(buffer, cnt, r * .8f, g * .8f, b * .8f);
                        addSideFullTextureD(buffer, cnt, r * .8f, g * .8f, b * .8f);
                        if (strip.isEmptyAt(i - 1, palette)) {
                            addSideFullTextureN(buffer, cnt, r * 1.2f, g * 1.2f, b * 1.2f);
                        }
                        if (strip.isEmptyAt(i + 1, palette)) {
                            addSideFullTextureS(buffer, cnt, r * 1.2f, g * 1.2f, b * 1.2f);
                        }
                        addSideFullTextureW(buffer, cnt, r, g, b);
                        addSideFullTextureE(buffer, cnt, r, g, b);
                    } else {
                        for (int c = 0 ; c < cnt ; c++) {
                            bd.render(buffer, c, r, g, b);
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

    private static RenderData.RenderElement beaconElement[] = null;
    private static RenderData.RenderElement beaconElementBeacon[] = null;

    private static RenderData.RenderElement getBeaconElement(Tessellator tessellator, VertexBuffer buffer, BeaconType type, boolean doBeacon) {
        if (beaconElement == null) {
            beaconElement = new RenderData.RenderElement[BeaconType.VALUES.length];
            beaconElementBeacon = new RenderData.RenderElement[BeaconType.VALUES.length];
            for (int i = 0 ; i < BeaconType.VALUES.length ; i++) {
                beaconElement[i] = null;
                beaconElementBeacon[i] = null;
            }
        }

        RenderData.RenderElement[] elements;
        if (doBeacon) {
            elements = ShapeRenderer.beaconElementBeacon;
        } else {
            elements = ShapeRenderer.beaconElement;
        }
        if (elements[type.ordinal()] == null) {
            elements[type.ordinal()] = new RenderData.RenderElement();
            elements[type.ordinal()].createRenderList(buffer);
            GlStateManager.glLineWidth(3);
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            float r = type.getR();
            float g = type.getG();
            float b = type.getB();

            double origOffsetX = buffer.xOffset;
            double origOffsetY = buffer.yOffset;
            double origOffsetZ = buffer.zOffset;
            buffer.setTranslation(origOffsetX, origOffsetY-.7f, origOffsetZ);
            addSideN(buffer, r, g, b, .3f);
            addSideS(buffer, r, g, b, .3f);
            addSideW(buffer, r, g, b, .3f);
            addSideE(buffer, r, g, b, .3f);
            addSideU(buffer, r, g, b, .3f);
            addSideD(buffer, r, g, b, .3f);
            buffer.setTranslation(origOffsetX, origOffsetY-.2f, origOffsetZ);
            addSideN(buffer, r, g, b, .2f);
            addSideS(buffer, r, g, b, .2f);
            addSideW(buffer, r, g, b, .2f);
            addSideE(buffer, r, g, b, .2f);
            addSideU(buffer, r, g, b, .2f);
            addSideD(buffer, r, g, b, .2f);

            if (doBeacon) {
                buffer.setTranslation(origOffsetX, origOffsetY+.2f, origOffsetZ);
                addSideN(buffer, r, g, b, .1f, ScannerConfiguration.locatorBeaconHeight);
                addSideS(buffer, r, g, b, .1f, ScannerConfiguration.locatorBeaconHeight);
                addSideW(buffer, r, g, b, .1f, ScannerConfiguration.locatorBeaconHeight);
                addSideE(buffer, r, g, b, .1f, ScannerConfiguration.locatorBeaconHeight);
            }

            buffer.setTranslation(origOffsetX, origOffsetY, origOffsetZ);

            elements[type.ordinal()].performRenderToList(tessellator, buffer);
        }
        return elements[type.ordinal()];
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

    public static void addSideFullTextureD(VertexBuffer buffer, int cnt, float r, float g, float b) {
        float a = 0.5f;
        buffer.pos(0, 0, 0).color(r, g, b, a).endVertex();
        buffer.pos(1, 0, 0).color(r, g, b, a).endVertex();
        buffer.pos(1, 0, cnt).color(r, g, b, a).endVertex();
        buffer.pos(0, 0, cnt).color(r, g, b, a).endVertex();
    }

    public static void addSideFullTextureU(VertexBuffer buffer, int cnt, float r, float g, float b) {
        float a = 0.5f;
        buffer.pos(0, 1, cnt).color(r, g, b, a).endVertex();
        buffer.pos(1, 1, cnt).color(r, g, b, a).endVertex();
        buffer.pos(1, 1, 0).color(r, g, b, a).endVertex();
        buffer.pos(0, 1, 0).color(r, g, b, a).endVertex();
    }

    public static void addSideFullTextureE(VertexBuffer buffer, int cnt, float r, float g, float b) {
        float a = 0.5f;
        buffer.pos(1, 0, 0).color(r, g, b, a).endVertex();
        buffer.pos(1, 1, 0).color(r, g, b, a).endVertex();
        buffer.pos(1, 1, cnt).color(r, g, b, a).endVertex();
        buffer.pos(1, 0, cnt).color(r, g, b, a).endVertex();
    }

    public static void addSideFullTextureW(VertexBuffer buffer, int cnt, float r, float g, float b) {
        float a = 0.5f;
        buffer.pos(0, 0, cnt).color(r, g, b, a).endVertex();
        buffer.pos(0, 1, cnt).color(r, g, b, a).endVertex();
        buffer.pos(0, 1, 0).color(r, g, b, a).endVertex();
        buffer.pos(0, 0, 0).color(r, g, b, a).endVertex();
    }

    public static void addSideFullTextureN(VertexBuffer buffer, int cnt, float r, float g, float b) {
        float a = 0.5f;
        buffer.pos(1, 1, 0).color(r, g, b, a).endVertex();
        buffer.pos(1, 0, 0).color(r, g, b, a).endVertex();
        buffer.pos(0, 0, 0).color(r, g, b, a).endVertex();
        buffer.pos(0, 1, 0).color(r, g, b, a).endVertex();
    }

    public static void addSideFullTextureS(VertexBuffer buffer, int cnt, float r, float g, float b) {
        float a = 0.5f;
        buffer.pos(1, 0, cnt).color(r, g, b, a).endVertex();
        buffer.pos(1, 1, cnt).color(r, g, b, a).endVertex();
        buffer.pos(0, 1, cnt).color(r, g, b, a).endVertex();
        buffer.pos(0, 0, cnt).color(r, g, b, a).endVertex();
    }




    public static void addSideD(VertexBuffer buffer, float r, float g, float b, float size) {
        float a = 0.5f;
        float l = -size;
        float h = size;
        buffer.pos(l, l, l).color(r, g, b, a).endVertex();
        buffer.pos(h, l, l).color(r, g, b, a).endVertex();
        buffer.pos(h, l, h).color(r, g, b, a).endVertex();
        buffer.pos(l, l, h).color(r, g, b, a).endVertex();
    }

    public static void addSideU(VertexBuffer buffer, float r, float g, float b, float size) {
        float a = 0.5f;
        float l = -size;
        float h = size;
        buffer.pos(l, h, h).color(r, g, b, a).endVertex();
        buffer.pos(h, h, h).color(r, g, b, a).endVertex();
        buffer.pos(h, h, l).color(r, g, b, a).endVertex();
        buffer.pos(l, h, l).color(r, g, b, a).endVertex();
    }

    public static void addSideE(VertexBuffer buffer, float r, float g, float b, float size) {
        float a = 0.5f;
        float l = -size;
        float h = size;
        buffer.pos(h, l, l).color(r, g, b, a).endVertex();
        buffer.pos(h, h, l).color(r, g, b, a).endVertex();
        buffer.pos(h, h, h).color(r, g, b, a).endVertex();
        buffer.pos(h, l, h).color(r, g, b, a).endVertex();
    }

    public static void addSideW(VertexBuffer buffer, float r, float g, float b, float size) {
        float a = 0.5f;
        float l = -size;
        float h = size;
        buffer.pos(l, l, h).color(r, g, b, a).endVertex();
        buffer.pos(l, h, h).color(r, g, b, a).endVertex();
        buffer.pos(l, h, l).color(r, g, b, a).endVertex();
        buffer.pos(l, l, l).color(r, g, b, a).endVertex();
    }

    public static void addSideN(VertexBuffer buffer, float r, float g, float b, float size) {
        float a = 0.5f;
        float l = -size;
        float h = size;
        buffer.pos(h, h, l).color(r, g, b, a).endVertex();
        buffer.pos(h, l, l).color(r, g, b, a).endVertex();
        buffer.pos(l, l, l).color(r, g, b, a).endVertex();
        buffer.pos(l, h, l).color(r, g, b, a).endVertex();
    }

    public static void addSideS(VertexBuffer buffer, float r, float g, float b, float size) {
        float a = 0.5f;
        float l = -size;
        float h = size;
        buffer.pos(h, l, h).color(r, g, b, a).endVertex();
        buffer.pos(h, h, h).color(r, g, b, a).endVertex();
        buffer.pos(l, h, h).color(r, g, b, a).endVertex();
        buffer.pos(l, l, h).color(r, g, b, a).endVertex();
    }





    public static void addSideE(VertexBuffer buffer, float r, float g, float b, float size, float height) {
        float a = 0.5f;
        float l = -size;
        float h = size;
        buffer.pos(h, 0, l).color(r, g, b, a).endVertex();
        buffer.pos(h, height, l).color(r, g, b, a).endVertex();
        buffer.pos(h, height, h).color(r, g, b, a).endVertex();
        buffer.pos(h, 0, h).color(r, g, b, a).endVertex();
    }

    public static void addSideW(VertexBuffer buffer, float r, float g, float b, float size, float height) {
        float a = 0.5f;
        float l = -size;
        float h = size;
        buffer.pos(l, 0, h).color(r, g, b, a).endVertex();
        buffer.pos(l, height, h).color(r, g, b, a).endVertex();
        buffer.pos(l, height, l).color(r, g, b, a).endVertex();
        buffer.pos(l, 0, l).color(r, g, b, a).endVertex();
    }

    public static void addSideN(VertexBuffer buffer, float r, float g, float b, float size, float height) {
        float a = 0.5f;
        float l = -size;
        float h = size;
        buffer.pos(h, height, l).color(r, g, b, a).endVertex();
        buffer.pos(h, 0, l).color(r, g, b, a).endVertex();
        buffer.pos(l, 0, l).color(r, g, b, a).endVertex();
        buffer.pos(l, height, l).color(r, g, b, a).endVertex();
    }

    public static void addSideS(VertexBuffer buffer, float r, float g, float b, float size, float height) {
        float a = 0.5f;
        float l = -size;
        float h = size;
        buffer.pos(h, 0, h).color(r, g, b, a).endVertex();
        buffer.pos(h, height, h).color(r, g, b, a).endVertex();
        buffer.pos(l, height, h).color(r, g, b, a).endVertex();
        buffer.pos(l, 0, h).color(r, g, b, a).endVertex();
    }

}
