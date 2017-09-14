package mcjty.rftools.blocks.shaper;

import com.google.common.collect.AbstractIterator;
import gnu.trove.iterator.TLongIterator;
import gnu.trove.set.hash.TLongHashSet;
import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.Button;
import mcjty.lib.gui.widgets.ChoiceLabel;
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
import org.lwjgl.opengl.GL11;

import java.awt.Rectangle;
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
            configButton[i].setLayoutHint(new PositionalLayout.PositionalHint(2, 7 + i*18+2, 14, 12));
            int finalI = i;
            configButton[i].addButtonEvent(parent -> openCardGui(finalI));
            toplevel.addChild(configButton[i]);
        }
        outConfigButton = new Button(mc, this).setText("?");
        outConfigButton.setLayoutHint(new PositionalLayout.PositionalHint(2, 200+2, 14, 12));
        outConfigButton.addButtonEvent(parent -> openCardGui(-1));
        toplevel.addChild(outConfigButton);

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
        if (keyCode == Keyboard.KEY_PRIOR) {
            scale += .2f;
            System.out.println("scale = " + scale);
        } else if (keyCode == Keyboard.KEY_NEXT) {
            scale -= .2f;
            System.out.println("scale = " + scale);
        } else if (keyCode == Keyboard.KEY_W) {
            dy -= 5f;
            System.out.println("dy = " + dy);
        } else if (keyCode == Keyboard.KEY_S) {
            dy += 5f;
            System.out.println("dy = " + dy);
        } else if (keyCode == Keyboard.KEY_A) {
            dx -= 5f;
            System.out.println("dx = " + dx);
        } else if (keyCode == Keyboard.KEY_D) {
            dx += 5f;
            System.out.println("dx = " + dx);
        } else if (keyCode == Keyboard.KEY_R) {
            xangle = 0;
            yangle = 0;
            zangle = 0;
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        drawWindow();

        Slot slot = inventorySlots.getSlot(ShaperContainer.SLOT_OUT);
        if (slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            if (ItemStackTools.isValid(stack)) {
                renderShape(stack);
            }
        }
    }

    private void renderShape(ItemStack stack) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(dx, dy, 100);
        GlStateManager.rotate(xangle, 1f, 0, 0); xangle += .16f;
        GlStateManager.rotate(yangle, 0, 1f, 0); yangle += .09f;
        GlStateManager.rotate(zangle, 0, 0, 1f); zangle += .31f;
        GlStateManager.scale(scale, scale, scale);

        GlStateManager.disableBlend();
//        GlStateManager.disableDepth();
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
                positions.add(coordinate.toLong());
                return true;
            }

            @Override
            public int size() {
                return 0;
            }
        }, ShapeCardItem.MAXIMUM_COUNT+1, false, null);

        renderFaces(stack, tessellator, buffer, base, shape, offset, clamped, positions);
        renderOutline(stack, tessellator, buffer, base, shape, offset, clamped, positions);

        GlStateManager.popMatrix();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        RenderHelper.enableGUIStandardItemLighting();
    }

    private boolean isPositionEnclosed(TLongHashSet positions, BlockPos coordinate) {
        return positions.contains(coordinate.up().toLong()) &&
                positions.contains(coordinate.down().toLong()) &&
                positions.contains(coordinate.east().toLong()) &&
                positions.contains(coordinate.west().toLong()) &&
                positions.contains(coordinate.south().toLong()) &&
                positions.contains(coordinate.north().toLong());
    }

    private void renderOutline(ItemStack stack, Tessellator tessellator, final VertexBuffer buffer, final BlockPos base, Shape shape, BlockPos offset, BlockPos clamped,
                               TLongHashSet positions) {
        GlStateManager.glLineWidth(1);
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        TLongIterator iterator = positions.iterator();
        while (iterator.hasNext()) {
            long p = iterator.next();
            BlockPos coordinate = BlockPos.fromLong(p);
            if (!isPositionEnclosed(positions, coordinate)) {
                mcjty.lib.gui.RenderHelper.renderHighLightedBlocksOutline(buffer,
                        base.getX() + coordinate.getX(), base.getY() + coordinate.getY(), base.getZ() + coordinate.getZ(),
                        1, 1, 1, 1.0f);
            }
        }
        tessellator.draw();
    }

    private void renderFaces(ItemStack stack, Tessellator tessellator, final VertexBuffer buffer, final BlockPos base, Shape shape, BlockPos offset, BlockPos clamped,
                             TLongHashSet positions) {
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        TLongIterator iterator = positions.iterator();
        while (iterator.hasNext()) {
            long p = iterator.next();
            BlockPos coordinate = BlockPos.fromLong(p);
            if (!isPositionEnclosed(positions, coordinate)) {
                float x = base.getX() + coordinate.getX();
                float y = base.getY() + coordinate.getY();
                float z = base.getZ() + coordinate.getZ();
                buffer.setTranslation(buffer.xOffset + x, buffer.yOffset + y, buffer.zOffset + z);

                addSideFullTexture(buffer, EnumFacing.UP.ordinal(), 1f, 0, 0, .5f, 1);
                addSideFullTexture(buffer, EnumFacing.DOWN.ordinal(), 1f, 0, 0, 0, 1);
                addSideFullTexture(buffer, EnumFacing.NORTH.ordinal(), 1f, 0, 0, 1, 0);
                addSideFullTexture(buffer, EnumFacing.SOUTH.ordinal(), 1f, 0, .5f, 1, 0);
                addSideFullTexture(buffer, EnumFacing.WEST.ordinal(), 1f, 0, 1, 0, 0);
                addSideFullTexture(buffer, EnumFacing.EAST.ordinal(), 1f, 0, 1, .5f, 0);
                buffer.setTranslation(buffer.xOffset - x, buffer.yOffset - y, buffer.zOffset - z);
            }
        }
        tessellator.draw();
    }

    private static final Quad[] quads = new Quad[] {
            new Quad(new Vt(0, 0, 0), new Vt(1, 0, 0), new Vt(1, 0, 1), new Vt(0, 0, 1)),       // DOWN
            new Quad(new Vt(0, 1, 1), new Vt(1, 1, 1), new Vt(1, 1, 0), new Vt(0, 1, 0)),       // UP
            new Quad(new Vt(1, 1, 0), new Vt(1, 0, 0), new Vt(0, 0, 0), new Vt(0, 1, 0)),       // NORTH
            new Quad(new Vt(1, 0, 1), new Vt(1, 1, 1), new Vt(0, 1, 1), new Vt(0, 0, 1)),       // SOUTH
            new Quad(new Vt(0, 0, 1), new Vt(0, 1, 1), new Vt(0, 1, 0), new Vt(0, 0, 0)),       // WEST
            new Quad(new Vt(1, 0, 0), new Vt(1, 1, 0), new Vt(1, 1, 1), new Vt(1, 0, 1)),       // EAST
    };


    public static void addSideFullTexture(VertexBuffer buffer, int side, float mult, float offset, float r, float g, float b) {
        int brightness = 240;
        int b1 = brightness >> 16 & 65535;
        int b2 = brightness & 65535;
        float u1 = 0;
        float v1 = 0;
        float u2 = 1;
        float v2 = 1;
        Quad quad = quads[side];
        buffer.pos(quad.v1.x * mult + offset, quad.v1.y * mult + offset, quad.v1.z * mult + offset).color(r, g, b, 1.0f).endVertex();
        buffer.pos(quad.v2.x * mult + offset, quad.v2.y * mult + offset, quad.v2.z * mult + offset).color(r, g, b, 1.0f).endVertex();
        buffer.pos(quad.v3.x * mult + offset, quad.v3.y * mult + offset, quad.v3.z * mult + offset).color(r, g, b, 1.0f).endVertex();
        buffer.pos(quad.v4.x * mult + offset, quad.v4.y * mult + offset, quad.v4.z * mult + offset).color(r, g, b, 1.0f).endVertex();
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

        public Quad rotate(EnumFacing direction) {
            switch (direction) {
                case NORTH: return new Quad(v4, v1, v2, v3);
                case EAST: return new Quad(v3, v4, v1, v2);
                case SOUTH: return new Quad(v2, v3, v4, v1);
                case WEST: return this;
                default: return this;
            }
        }
    }

}
