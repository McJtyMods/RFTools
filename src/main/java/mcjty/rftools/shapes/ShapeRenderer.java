package mcjty.rftools.shapes;

import gnu.trove.iterator.TLongIterator;
import gnu.trove.set.hash.TLongHashSet;
import mcjty.lib.container.GenericGuiContainer;
import mcjty.rftools.items.builder.ShapeCardItem;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Set;
import java.util.zip.Adler32;

public class ShapeRenderer {

    private int prevX = -1;
    private int prevY = -1;

    private float scale = 3.0f;
    private float dx = 230.0f;
    private float dy = 100.0f;
    private float xangle = 0.0f;
    private float yangle = 0.0f;
    private float zangle = 0.0f;

    private int glList = -1;
    private long checksum = -1;


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


    public void renderShape(GenericGuiContainer gui, ItemStack stack, int x, int y, boolean showAxis, boolean showOuter) {
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

        Shape shape = ShapeCardItem.getShape(stack);
        boolean solid = ShapeCardItem.isSolid(stack);
        BlockPos dimension = ShapeCardItem.getDimension(stack);
        BlockPos clamped = new BlockPos(Math.min(dimension.getX(), 512), Math.min(dimension.getY(), 256), Math.min(dimension.getZ(), 512));

        GL11.glEnable(GL11.GL_SCISSOR_TEST);

        renderFaces(tessellator, buffer, stack, shape, solid, clamped);
//        renderOutline(tessellator, buffer, positions);
        renderHelpers(tessellator, buffer, dimension.getX()/2.0f, dimension.getY()/2.0f, dimension.getZ()/2.0f, showAxis, showOuter);

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
    }

    private void renderHelpers(Tessellator tessellator, VertexBuffer buffer, float xlen, float ylen, float zlen, boolean showAxis, boolean showOuter) {
        // X, Y, Z axis
        if (showAxis) {
            ShapeRenderer.renderAxis(tessellator, buffer, xlen, ylen, zlen);
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

    static void renderOuterBox(Tessellator tessellator, VertexBuffer buffer, float xlen, float ylen, float zlen) {
        GlStateManager.glLineWidth(1.0f);
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(-xlen, -ylen, -zlen).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(xlen, -ylen, -zlen).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(-xlen, -ylen, -zlen).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(-xlen, ylen, -zlen).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(-xlen, -ylen, -zlen).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(-xlen, -ylen, zlen).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(xlen, ylen, zlen).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(-xlen, ylen, zlen).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(xlen, ylen, zlen).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(xlen, -ylen, zlen).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(xlen, ylen, zlen).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(xlen, ylen, -zlen).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(xlen, -ylen, -zlen).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(xlen, -ylen, zlen).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(xlen, -ylen, -zlen).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(xlen, ylen, -zlen).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(-xlen, ylen, -zlen).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(xlen, ylen, -zlen).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(-xlen, ylen, -zlen).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(-xlen, ylen, zlen).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(-xlen, ylen, zlen).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(-xlen, -ylen, zlen).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(-xlen, -ylen, zlen).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(xlen, -ylen, zlen).color(1f, 1f, 1f, 1f).endVertex();
        tessellator.draw();
    }

    static void renderAxis(Tessellator tessellator, VertexBuffer buffer, float xlen, float ylen, float zlen) {
        GlStateManager.glLineWidth(2.5f);
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(0, 0, 0).color(1f, 0f, 0f, 1f).endVertex();
        buffer.pos(xlen, 0, 0).color(1f, 0f, 0f, 1f).endVertex();
        buffer.pos(0, 0, 0).color(0f, 1f, 0f, 1f).endVertex();
        buffer.pos(0, ylen, 0).color(0f, 1f, 0f, 1f).endVertex();
        buffer.pos(0, 0, 0).color(0f, 0f, 1f, 1f).endVertex();
        buffer.pos(0, 0, zlen).color(0f, 0f, 1f, 1f).endVertex();
        tessellator.draw();
    }

    private static TLongHashSet getPositions(ItemStack stack, Shape shape, boolean solid, BlockPos clamped) {
        TLongHashSet positions = new TLongHashSet();
        ShapeCardItem.composeShape(stack, shape, solid, null, new BlockPos(0, 0, 0), clamped, new BlockPos(0, 0, 0), new AbstractMap<BlockPos, IBlockState>() {
            @Override
            public Set<Entry<BlockPos, IBlockState>> entrySet() {
                return Collections.emptySet();
            }

            @Override
            public IBlockState put(BlockPos key, IBlockState value) {
                positions.add(new BlockPos(key.getX(), key.getY(), -key.getZ()).toLong());
                return value;
            }

            @Override
            public int size() {
                return 0;
            }
        }, ShapeCardItem.MAXIMUM_COUNT+1, false, null);
        return positions;
    }

    static boolean isPositionEnclosed(TLongHashSet positions, BlockPos coordinate) {
        return positions.contains(coordinate.up().toLong()) &&
                positions.contains(coordinate.down().toLong()) &&
                positions.contains(coordinate.east().toLong()) &&
                positions.contains(coordinate.west().toLong()) &&
                positions.contains(coordinate.south().toLong()) &&
                positions.contains(coordinate.north().toLong());
    }

    public static void renderOutline(Tessellator tessellator, final VertexBuffer buffer,
                                      TLongHashSet positions) {
        GlStateManager.glLineWidth(1);
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        TLongIterator iterator = positions.iterator();
        while (iterator.hasNext()) {
            long p = iterator.next();
            BlockPos coordinate = BlockPos.fromLong(p);
            if (!isPositionEnclosed(positions, coordinate)) {
                renderHighLightedBlocksOutline(buffer,
                        coordinate.getX(), coordinate.getY(), coordinate.getZ(),
                        .5f ,5f ,5f, .5f);
            }
        }

        tessellator.draw();
    }

    private static void renderHighLightedBlocksOutline(VertexBuffer buffer, float mx, float my, float mz, float r, float g, float b, float a) {
        buffer.pos(mx, my, mz).color(r, g, b, a).endVertex();
        buffer.pos(mx+1, my, mz).color(r, g, b, a).endVertex();
        buffer.pos(mx, my, mz).color(r, g, b, a).endVertex();
        buffer.pos(mx, my+1, mz).color(r, g, b, a).endVertex();
        buffer.pos(mx, my, mz).color(r, g, b, a).endVertex();
        buffer.pos(mx, my, mz+1).color(r, g, b, a).endVertex();
        buffer.pos(mx+1, my+1, mz+1).color(r, g, b, a).endVertex();
        buffer.pos(mx, my+1, mz+1).color(r, g, b, a).endVertex();
        buffer.pos(mx+1, my+1, mz+1).color(r, g, b, a).endVertex();
        buffer.pos(mx+1, my, mz+1).color(r, g, b, a).endVertex();
        buffer.pos(mx+1, my+1, mz+1).color(r, g, b, a).endVertex();
        buffer.pos(mx+1, my+1, mz).color(r, g, b, a).endVertex();

        buffer.pos(mx, my+1, mz).color(r, g, b, a).endVertex();
        buffer.pos(mx, my+1, mz+1).color(r, g, b, a).endVertex();
        buffer.pos(mx, my+1, mz).color(r, g, b, a).endVertex();
        buffer.pos(mx+1, my+1, mz).color(r, g, b, a).endVertex();

        buffer.pos(mx+1, my, mz).color(r, g, b, a).endVertex();
        buffer.pos(mx+1, my, mz+1).color(r, g, b, a).endVertex();
        buffer.pos(mx+1, my, mz).color(r, g, b, a).endVertex();
        buffer.pos(mx+1, my+1, mz).color(r, g, b, a).endVertex();

        buffer.pos(mx, my, mz+1).color(r, g, b, a).endVertex();
        buffer.pos(mx+1, my, mz+1).color(r, g, b, a).endVertex();
        buffer.pos(mx, my, mz+1).color(r, g, b, a).endVertex();
        buffer.pos(mx, my+1, mz+1).color(r, g, b, a).endVertex();

    }

    private long calculateChecksum(ItemStack stack) {
        return ShapeCardItem.getCheck(stack);
    }

    void renderFaces(Tessellator tessellator, final VertexBuffer buffer,
                     ItemStack stack, Shape shape, boolean solid, BlockPos clamped) {

        long check = calculateChecksum(stack);

        if (glList == -1 || check != checksum) {
            if (checksum != check) {
                System.out.println("check = " + check);
            }
            checksum = check;
            invalidateGlList();
            glList = GLAllocation.generateDisplayLists(1);
            GlStateManager.glNewList(glList, GL11.GL_COMPILE);

            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
//        GlStateManager.enableBlend();
//        GlStateManager.enableAlpha();

            TLongHashSet positions = ShapeRenderer.getPositions(stack, shape, solid, clamped);

            TLongIterator iterator = positions.iterator();
            while (iterator.hasNext()) {
                long p = iterator.next();
                BlockPos coordinate = BlockPos.fromLong(p);
                if (!isPositionEnclosed(positions, coordinate)) {
                    int x = coordinate.getX();
                    int y = coordinate.getY();
                    int z = coordinate.getZ();

                    buffer.setTranslation(buffer.xOffset + x, buffer.yOffset + y, buffer.zOffset + z);
                    float d = .2f;
                    float l = ((x + y + z) & 1) == 1 ? .9f : .6f;
                    if (!positions.contains(coordinate.up().toLong())) {
                        addSideFullTexture(buffer, EnumFacing.UP.ordinal(), d, l, d);
                    }
                    if (!positions.contains(coordinate.down().toLong())) {
                        addSideFullTexture(buffer, EnumFacing.DOWN.ordinal(), d, l, d);
                    }
                    if (!positions.contains(coordinate.north().toLong())) {
                        addSideFullTexture(buffer, EnumFacing.NORTH.ordinal(), d, d, l);
                    }
                    if (!positions.contains(coordinate.south().toLong())) {
                        addSideFullTexture(buffer, EnumFacing.SOUTH.ordinal(), d, d, l);
                    }
                    if (!positions.contains(coordinate.west().toLong())) {
                        addSideFullTexture(buffer, EnumFacing.WEST.ordinal(), l, d, d);
                    }
                    if (!positions.contains(coordinate.east().toLong())) {
                        addSideFullTexture(buffer, EnumFacing.EAST.ordinal(), l, d, d);
                    }
                    buffer.setTranslation(buffer.xOffset - x, buffer.yOffset - y, buffer.zOffset - z);
                }
            }
            tessellator.draw();
            GlStateManager.glEndList();
        }

        GlStateManager.callList(glList);

//        GlStateManager.disableBlend();
//        GlStateManager.disableAlpha();

    }

    private static void setupScissor(GuiContainer gui) {
        Minecraft mc = Minecraft.getMinecraft();

        final ScaledResolution scaledresolution = new ScaledResolution(mc);
        int xScale = scaledresolution.getScaledWidth();
        int yScale = scaledresolution.getScaledHeight();
        int sx = (gui.getGuiLeft() + 84) * mc.displayWidth / xScale;
        int sy = (mc.displayHeight) - (gui.getGuiTop() + 136) * mc.displayHeight / yScale;
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
