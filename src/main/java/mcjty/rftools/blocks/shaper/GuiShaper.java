package mcjty.rftools.blocks.shaper;

import com.google.common.collect.AbstractIterator;
import gnu.trove.iterator.TLongIterator;
import gnu.trove.set.hash.TLongHashSet;
import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.WindowManager;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.Button;
import mcjty.lib.gui.widgets.*;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.tools.ItemStackTools;
import mcjty.lib.tools.MinecraftTools;
import mcjty.rftools.RFTools;
import mcjty.rftools.items.builder.Shape;
import mcjty.rftools.items.builder.*;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.block.state.IBlockState;
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
import java.util.*;

public class GuiShaper extends GenericGuiContainer<ShaperTileEntity> {
    public static final int SIDEWIDTH = 80;
    public static final int SHAPER_WIDTH = 256;
    public static final int SHAPER_HEIGHT = 238;

    private static final ResourceLocation sideBackground = new ResourceLocation(RFTools.MODID, "textures/gui/sidegui.png");
    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/shaper.png");
    private static final ResourceLocation guiElements = new ResourceLocation(RFTools.MODID, "textures/gui/guielements.png");

    private ChoiceLabel operationLabels[] = new ChoiceLabel[ShaperContainer.SLOT_COUNT];
    private ChoiceLabel rotationLabels[] = new ChoiceLabel[ShaperContainer.SLOT_COUNT];
    private ToggleButton flipButtons[] = new ToggleButton[ShaperContainer.SLOT_COUNT];
    private Button configButton[] = new Button[ShaperContainer.SLOT_COUNT];
    private Button outConfigButton;
    private ToggleButton showAxis;
    private ToggleButton showOuter;

    // For GuiShapeCard: the current card to edit
    public static BlockPos shaperBlock = null;
    public static int shaperStackSlot = 0;

    private Window sideWindow;


    public GuiShaper(ShaperTileEntity shaperTileEntity, ShaperContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, shaperTileEntity, container, RFTools.GUI_MANUAL_MAIN, "shaper");

        xSize = SHAPER_WIDTH;
        ySize = SHAPER_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        Panel toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout());

        ShapeModifier[] modifiers = tileEntity.getModifiers();

        operationLabels[0] = null;
        for (int i = 0 ; i < ShaperContainer.SLOT_COUNT ; i++) {
            operationLabels[i] = new ChoiceLabel(mc, this).addChoices(
                    ShapeOperation.UNION.getCode(),
                    ShapeOperation.SUBTRACT.getCode(),
                    ShapeOperation.INTERSECT.getCode());
            for (ShapeOperation operation : ShapeOperation.values()) {
                operationLabels[i].setChoiceTooltip(operation.getCode(), operation.getDescription());
            }
            operationLabels[i].setLayoutHint(new PositionalLayout.PositionalHint(55, 7 + i*18, 26, 16));
            operationLabels[i].setChoice(modifiers[i].getOperation().getCode());
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

        showAxis = new ToggleButton(mc, this).setCheckMarker(true).setText("Axis").setLayoutHint(new PositionalLayout.PositionalHint(3, 176, 38, 16));
        showAxis.setPressed(true);
        toplevel.addChild(showAxis);
        showOuter = new ToggleButton(mc, this).setCheckMarker(true).setText("Box").setLayoutHint(new PositionalLayout.PositionalHint(42, 176, 38, 16));
        showOuter.setPressed(true);
        toplevel.addChild(showOuter);

        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        Panel sidePanel = new Panel(mc, this).setLayout(new PositionalLayout()).setBackground(sideBackground);
        String[] tt = {
                "Drag left mouse button to rotate",
                "Shift drag left mouse to pan",
                "Use mouse wheel to zoom in/out",
                "Use middle click to reset rotation" };
        sidePanel.addChild(new Label<>(mc, this).setText("E").setColor(0xffff0000).setTooltips(tt).setLayoutHint(new PositionalLayout.PositionalHint(5, 175, 15, 15)));
        sidePanel.addChild(new Label<>(mc, this).setText("W").setColor(0xffff0000).setTooltips(tt).setLayoutHint(new PositionalLayout.PositionalHint(40, 175, 15, 15)));
        sidePanel.addChild(new Label<>(mc, this).setText("U").setColor(0xff00bb00).setTooltips(tt).setLayoutHint(new PositionalLayout.PositionalHint(5, 190, 15, 15)));
        sidePanel.addChild(new Label<>(mc, this).setText("D").setColor(0xff00bb00).setTooltips(tt).setLayoutHint(new PositionalLayout.PositionalHint(40, 190, 15, 15)));
        sidePanel.addChild(new Label<>(mc, this).setText("N").setColor(0xff0000ff).setTooltips(tt).setLayoutHint(new PositionalLayout.PositionalHint(5, 205, 15, 15)));
        sidePanel.addChild(new Label<>(mc, this).setText("S").setColor(0xff0000ff).setTooltips(tt).setLayoutHint(new PositionalLayout.PositionalHint(40, 205, 15, 15)));

        for (int i = 0 ; i < ShaperContainer.SLOT_COUNT ; i++) {
            ToggleButton flip = new ToggleButton(mc, this).setText("Flip").setCheckMarker(true).setLayoutHint(new PositionalLayout.PositionalHint(6, 7 + i*18, 35, 16));
            flip.setPressed(modifiers[i].isFlipY());
            flip.addButtonEvent(parent -> update());
            sidePanel.addChild(flip);
            flipButtons[i] = flip;
            ChoiceLabel rot = new ChoiceLabel(mc, this).addChoices("None", "X", "Y", "Z").setChoice("None").setLayoutHint(new PositionalLayout.PositionalHint(45, 7 + i*18, 35, 16));
            rot.setChoice(modifiers[i].getRotation().getCode());
            rot.addChoiceEvent((parent, newChoice) -> update());
            sidePanel.addChild(rot);
            rotationLabels[i] = rot;
        }

        sidePanel.setBounds(new Rectangle(guiLeft-SIDEWIDTH, guiTop, SIDEWIDTH, ySize));
        sideWindow = new Window(this, sidePanel);

        window = new Window(this, toplevel);
    }


    @Override
    protected void registerWindows(WindowManager mgr) {
        super.registerWindows(mgr);
        mgr.addWindow(sideWindow);
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
        ShapeModifier[] modifiers = new ShapeModifier[ShaperContainer.SLOT_COUNT];
        for (int i = 0 ; i < ShaperContainer.SLOT_COUNT ; i++) {
            ShapeOperation op = ShapeOperation.getByName(operationLabels[i].getCurrentChoice());
            ShapeRotation rot = ShapeRotation.getByName(rotationLabels[i].getCurrentChoice());
            modifiers[i] = new ShapeModifier(op, flipButtons[i].isPressed(), rot);
        }
        network.sendToServer(new PacketSendShaperData(tileEntity.getPos(), modifiers));
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
//            scale -= 1f;
            scale *= .6;
            if (scale <= 0.1) {
                scale = .1f;
            }
        } else if (dwheel > 0) {
//            scale += 1f;
            scale *= 1.4;
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
        int sy = (mc.displayHeight) - (guiTop + 136) * mc.displayHeight / yScale;
        int sw = 161 * mc.displayWidth / xScale;
        int sh = 130 * mc.displayHeight / yScale;

        GL11.glScissor(sx, sy, sw, sh);
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

        TLongHashSet positions = getPositions(stack, shape, solid, clamped);

        GL11.glEnable(GL11.GL_SCISSOR_TEST);

        renderFaces(tessellator, buffer, positions);
//        renderOutline(tessellator, buffer, positions);
        renderAxis(tessellator, buffer, dimension.getX()/2.0f, dimension.getY()/2.0f, dimension.getZ()/2.0f);

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

    private void renderAxis(Tessellator tessellator, VertexBuffer buffer, float xlen, float ylen, float zlen) {
        // X, Y, Z axis
        if (showAxis.isPressed()) {
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

        if (showOuter.isPressed()) {
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
    }

    private TLongHashSet getPositions(ItemStack stack, Shape shape, boolean solid, BlockPos clamped) {
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
//        GlStateManager.enableBlend();
//        GlStateManager.enableAlpha();

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
                float l = ((x+y+z) & 1) == 1 ? .9f : .6f;
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
