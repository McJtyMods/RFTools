package mcjty.rftools.blocks.shaper;

import com.google.common.collect.AbstractIterator;
import gnu.trove.iterator.TLongIterator;
import gnu.trove.set.hash.TLongHashSet;
import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.Button;
import mcjty.lib.gui.widgets.ChoiceLabel;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.tools.ItemStackTools;
import mcjty.lib.tools.MinecraftTools;
import mcjty.rftools.RFTools;
import mcjty.rftools.items.builder.Shape;
import mcjty.rftools.items.builder.ShapeCardItem;
import mcjty.rftools.items.builder.ShapeOperation;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;
import java.util.AbstractCollection;
import java.util.Iterator;

public class GuiShaper extends GenericGuiContainer<ShaperTileEntity> {
    public static final int SHAPER_WIDTH = 256;
    public static final int SHAPER_HEIGHT = 238;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/shaper.png");
    private static final ResourceLocation guiElements = new ResourceLocation(RFTools.MODID, "textures/gui/guielements.png");

    private ChoiceLabel operationLabels[] = new ChoiceLabel[ShaperContainer.SLOT_COUNT];
    private Button configButton[] = new Button[ShaperContainer.SLOT_COUNT];
    private Button outConfigButton;

    // For GuiShapeCard: the current card to edit
    public static BlockPos shaperBlock = null;
    public static int shaperStackSlot = 0;

    public GuiShaper(ShaperTileEntity shaperTileEntity, ShaperContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, shaperTileEntity, container, RFTools.GUI_MANUAL_MAIN, "shaper");

        xSize = SHAPER_WIDTH;
        ySize = SHAPER_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        Panel toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout());

        ShapeOperation[] operations = tileEntity.getOperations();
        operationLabels[0] = null;
        for (int i = 0 ; i < ShaperContainer.SLOT_COUNT ; i++) {
            operationLabels[i] = new ChoiceLabel(mc, this).addChoices(
                    ShapeOperation.UNION.getCode(),
                    ShapeOperation.SUBTRACT.getCode(),
                    ShapeOperation.INTERSECT.getCode());
            for (ShapeOperation operation : ShapeOperation.values()) {
                operationLabels[i].setChoiceTooltip(operation.getCode(), operation.getDescription());
            }
            operationLabels[i].setLayoutHint(new PositionalLayout.PositionalHint(39, 7 + i*18, 40, 16));
            operationLabels[i].setChoice(operations[i].getCode());
            operationLabels[i].addChoiceEvent((parent, newChoice) -> update());
            toplevel.addChild(operationLabels[i]);
        }
        operationLabels[0].setEnabled(false);

        for (int i = 0 ; i < ShaperContainer.SLOT_COUNT ; i++) {
            configButton[i] = new Button(mc, this).setText("?");
            configButton[i].setLayoutHint(new PositionalLayout.PositionalHint(3, 7 + i*18+2, 13, 12));
            int finalI = i;
            configButton[i].addButtonEvent(parent -> openCardGui(finalI));
            configButton[i].setTooltips("Click to open the card gui");
            toplevel.addChild(configButton[i]);
        }
        outConfigButton = new Button(mc, this).setText("?");
        outConfigButton.setLayoutHint(new PositionalLayout.PositionalHint(3, 200+2, 13, 12));
        outConfigButton.addButtonEvent(parent -> openCardGui(-1));
        outConfigButton.setTooltips("Click to open the card gui");
        toplevel.addChild(outConfigButton);

        String[] tt = {
                "Drag left mouse button to rotate",
                "Shift drag left mouse to pan",
                "Use mouse wheel to zoom in/out",
                "Use middle click to reset rotation" };
        toplevel.addChild(new Label<>(mc, this).setText("E/W").setColor(0xffff0000).setTooltips(tt).setLayoutHint(new PositionalLayout.PositionalHint(5, 145, 20, 15)));
        toplevel.addChild(new Label<>(mc, this).setText("U/D").setColor(0xff00ff00).setTooltips(tt).setLayoutHint(new PositionalLayout.PositionalHint(5, 160, 20, 15)));
        toplevel.addChild(new Label<>(mc, this).setText("N/S").setColor(0xff0000ff).setTooltips(tt).setLayoutHint(new PositionalLayout.PositionalHint(5, 175, 20, 15)));

        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);
    }

    float scale = 3.0f;
    float dx = 230.0f;
    float dy = 100.0f;
    float xangle = 0.0f;
    float yangle = 0.0f;
    float zangle = 0.0f;

    private void openCardGui(int i) {
        int slot;
        if (i == -1) {
            slot = ShaperContainer.SLOT_OUT;
        } else {
            slot = ShaperContainer.SLOT_TABS+i;
        }
        ItemStack cardStack = inventorySlots.getSlot(slot).getStack();
        if (ItemStackTools.isValid(cardStack)) {
            EntityPlayerSP player = MinecraftTools.getPlayer(Minecraft.getMinecraft());
            shaperBlock = tileEntity.getPos();
            shaperStackSlot = slot;
            player.openGui(RFTools.instance, RFTools.GUI_SHAPECARD_SHAPER, player.getEntityWorld(), (int) player.posX, (int) player.posY, (int) player.posZ);
        }
    }

    private void update() {
        ShapeOperation[] operations = new ShapeOperation[ShaperContainer.SLOT_COUNT];
        operations[0] = ShapeOperation.UNION;
        for (int i = 1 ; i < ShaperContainer.SLOT_COUNT ; i++) {
            operations[i] = ShapeOperation.getByName(operationLabels[i].getCurrentChoice());
        }
        network.sendToServer(new PacketSendShaperData(tileEntity.getPos(), operations));
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
    }

    private int prevX = -1;
    private int prevY = -1;

    @Override
    protected void mouseClicked(int x, int y, int button) throws IOException {
        super.mouseClicked(x, y, button);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int x = Mouse.getEventX() * width / mc.displayWidth;
        int y = height - Mouse.getEventY() * height / mc.displayHeight - 1;
        x -= guiLeft;
        y -= guiTop;

        if (x >= 100 && y <= 120) {
            if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
                if (prevX != -1 && Mouse.isButtonDown(0)) {
                    dx += (x - prevX);
                    dy += (y - prevY);
                }
            } else {
                if (prevX != -1 && Mouse.isButtonDown(0)) {
                    yangle += (x - prevX);
                    xangle -= (y - prevY);
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
            scale -= 2f;
        } else if (dwheel > 0) {
            scale += 2f;
        }
    }

    @Override
    protected void mouseReleased(int x, int y, int state) {
        super.mouseReleased(x, y, state);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int x, int y) {
        drawWindow();

        Slot slot = inventorySlots.getSlot(ShaperContainer.SLOT_OUT);
        if (slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            if (ItemStackTools.isValid(stack)) {
                renderShape(stack, guiLeft, guiTop);
            }
        }
    }

    private void renderShape(ItemStack stack, int x, int y) {
        final ScaledResolution scaledresolution = new ScaledResolution(this.mc);
        int xScale = scaledresolution.getScaledWidth();
        int yScale = scaledresolution.getScaledHeight();

        int sx = (guiLeft + 84) * mc.displayWidth / xScale;
        int sy = (mc.displayHeight) - (guiTop + 135) * mc.displayHeight / yScale;
        int sw = 165 * mc.displayWidth / xScale;
        int sh = 130 * mc.displayHeight / yScale;

        GL11.glScissor(sx, sy, sw, sh);
        GlStateManager.pushMatrix();
        GlStateManager.translate(dx, dy, 100);
        GlStateManager.rotate(xangle, 1f, 0, 0); //xangle += .16f;
        GlStateManager.rotate(yangle, 0, 1f, 0); //yangle += .09f;
        GlStateManager.rotate(zangle, 0, 0, 1f); //zangle += .31f;
        GlStateManager.scale(scale, scale, scale);

        GlStateManager.disableBlend();
        GlStateManager.disableCull();
        GlStateManager.disableTexture2D();

        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer buffer = tessellator.getBuffer();

        BlockPos base = new BlockPos(0, -128, 0);
        Shape shape = ShapeCardItem.getShape(stack);
        boolean solid = ShapeCardItem.isSolid(stack);
        BlockPos dimension = ShapeCardItem.getDimension(stack);
        BlockPos offset = new BlockPos(0, 128, 0);
        BlockPos clamped = new BlockPos(Math.min(dimension.getX(), 512), Math.min(dimension.getY(), 256), Math.min(dimension.getZ(), 512));

        TLongHashSet positions = getPositions(stack, shape, solid, base, offset, clamped);

        GL11.glEnable(GL11.GL_SCISSOR_TEST);

        renderFaces(tessellator, buffer, positions);
        renderOutline(tessellator, buffer, positions);
        renderAxis(tessellator, buffer, dimension.getX()/2.0f, dimension.getY()/2.0f, dimension.getZ()/2.0f);

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        GlStateManager.popMatrix();

        GlStateManager.glLineWidth(3);
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(x+30, y+150, 0)  .color(1f, 0f, 0f, 1f).endVertex();
        buffer.pos(x+50, y+150, 0)  .color(1f, 0f, 0f, 1f).endVertex();
        buffer.pos(x+30, y+165, 0)  .color(0f, 1f, 0f, 1f).endVertex();
        buffer.pos(x+50, y+165, 0)  .color(0f, 1f, 0f, 1f).endVertex();
        buffer.pos(x+30, y+180, 0)  .color(0f, 0f, 1f, 1f).endVertex();
        buffer.pos(x+50, y+180, 0)  .color(0f, 0f, 1f, 1f).endVertex();
        tessellator.draw();


        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        RenderHelper.enableGUIStandardItemLighting();
    }

    private void renderAxis(Tessellator tessellator, VertexBuffer buffer, float xlen, float ylen, float zlen) {
        BlockPos base = new BlockPos(0, 0, 0);
        // X, Y, Z axis
        GlStateManager.glLineWidth(2.5f);
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(0, 0, 0)     .color(1f, 0f, 0f, 1f).endVertex();
        buffer.pos(xlen, 0, 0)  .color(1f, 0f, 0f, 1f).endVertex();
        buffer.pos(0, 0, 0)     .color(0f, 1f, 0f, 1f).endVertex();
        buffer.pos(0, ylen, 0)  .color(0f, 1f, 0f, 1f).endVertex();
        buffer.pos(0, 0, 0)     .color(0f, 0f, 1f, 1f).endVertex();
        buffer.pos(0, 0, zlen)  .color(0f, 0f, 1f, 1f).endVertex();
        tessellator.draw();
    }

    private TLongHashSet getPositions(ItemStack stack, Shape shape, boolean solid, BlockPos base, BlockPos offset, BlockPos clamped) {
        TLongHashSet positions = new TLongHashSet();
        ShapeCardItem.composeShape(stack, shape, solid, null, new BlockPos(0, 0, 0), clamped, offset, new AbstractCollection<BlockPos>() {
            @Override
            public Iterator<BlockPos> iterator() {
                return new AbstractIterator<BlockPos>() {
                    @Override
                    protected BlockPos computeNext() {
                        return null;
                    }
                };
            }

            @Override
            public boolean add(BlockPos coordinate) {
                positions.add(base.add(coordinate).toLong());
                return true;
            }

            @Override
            public int size() {
                return 0;
            }
        }, ShapeCardItem.MAXIMUM_COUNT+1, false, null);
        return positions;
    }

    private boolean isPositionEnclosed(TLongHashSet positions, BlockPos coordinate) {
        return positions.contains(coordinate.up().toLong()) &&
                positions.contains(coordinate.down().toLong()) &&
                positions.contains(coordinate.east().toLong()) &&
                positions.contains(coordinate.west().toLong()) &&
                positions.contains(coordinate.south().toLong()) &&
                positions.contains(coordinate.north().toLong());
    }

    private void renderOutline(Tessellator tessellator, final VertexBuffer buffer,
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

    private void renderFaces(Tessellator tessellator, final VertexBuffer buffer,
                             TLongHashSet positions) {
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();

        TLongIterator iterator = positions.iterator();
        while (iterator.hasNext()) {
            long p = iterator.next();
            BlockPos coordinate = BlockPos.fromLong(p);
            if (!isPositionEnclosed(positions, coordinate)) {
                float x = coordinate.getX();
                float y = coordinate.getY();
                float z = coordinate.getZ();

                buffer.setTranslation(buffer.xOffset + x, buffer.yOffset + y, buffer.zOffset + z);
                if (!positions.contains(coordinate.up().toLong())) {
                    addSideFullTexture(buffer, EnumFacing.UP.ordinal(), 0, 1, 0);
                }
                if (!positions.contains(coordinate.down().toLong())) {
                    addSideFullTexture(buffer, EnumFacing.DOWN.ordinal(), 0, 1, 0);
                }
                if (!positions.contains(coordinate.north().toLong())) {
                    addSideFullTexture(buffer, EnumFacing.NORTH.ordinal(), 0, 0, 1);
                }
                if (!positions.contains(coordinate.south().toLong())) {
                    addSideFullTexture(buffer, EnumFacing.SOUTH.ordinal(), 0, 0, 1);
                }
                if (!positions.contains(coordinate.west().toLong())) {
                    addSideFullTexture(buffer, EnumFacing.WEST.ordinal(), 1, 0, 0);
                }
                if (!positions.contains(coordinate.east().toLong())) {
                    addSideFullTexture(buffer, EnumFacing.EAST.ordinal(), 1, 0, 0);
                }
                buffer.setTranslation(buffer.xOffset - x, buffer.yOffset - y, buffer.zOffset - z);
            }
        }
        tessellator.draw();
        GlStateManager.disableBlend();
        GlStateManager.disableAlpha();

    }

    private static void bufpos(VertexBuffer buffer, float mx, float my, float mz, float r, float g, float b, float a) {
        buffer.pos(mx, my, mz).color(r, g, b, a).endVertex();
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

    private static final Quad[] QUADS = new Quad[] {
            new Quad(new Vt(0, 0, 0), new Vt(1, 0, 0), new Vt(1, 0, 1), new Vt(0, 0, 1)),       // DOWN
            new Quad(new Vt(0, 1, 1), new Vt(1, 1, 1), new Vt(1, 1, 0), new Vt(0, 1, 0)),       // UP
            new Quad(new Vt(1, 1, 0), new Vt(1, 0, 0), new Vt(0, 0, 0), new Vt(0, 1, 0)),       // NORTH
            new Quad(new Vt(1, 0, 1), new Vt(1, 1, 1), new Vt(0, 1, 1), new Vt(0, 0, 1)),       // SOUTH
            new Quad(new Vt(0, 0, 1), new Vt(0, 1, 1), new Vt(0, 1, 0), new Vt(0, 0, 0)),       // WEST
            new Quad(new Vt(1, 0, 0), new Vt(1, 1, 0), new Vt(1, 1, 1), new Vt(1, 0, 1)),       // EAST
    };


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
