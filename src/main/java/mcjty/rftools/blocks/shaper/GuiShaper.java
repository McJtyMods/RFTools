package mcjty.rftools.blocks.shaper;

import com.google.common.collect.AbstractIterator;
import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.tools.ItemStackTools;
import mcjty.rftools.RFTools;
import mcjty.rftools.items.builder.Shape;
import mcjty.rftools.items.builder.ShapeCardItem;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.input.Keyboard;
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

    public GuiShaper(ShaperTileEntity shaperTileEntity, ShaperContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, shaperTileEntity, container, RFTools.GUI_MANUAL_MAIN, "shaper");

        xSize = SHAPER_WIDTH;
        ySize = SHAPER_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        Panel toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout());
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);
    }

    float scale = 1.0f;

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        if (keyCode == Keyboard.KEY_UP) {
            scale += .1f;
            System.out.println("scale = " + scale);
        } else if (keyCode == Keyboard.KEY_DOWN) {
            scale -= .1f;
            System.out.println("scale = " + scale);
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
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.translate(20, -105, 1);
        GlStateManager.rotate(30.3f, 2.0f, 1.0f, 0);

        GlStateManager.disableBlend();
        GlStateManager.disableDepth();
        GlStateManager.disableTexture2D();

        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer buffer = tessellator.getBuffer();

        GlStateManager.glLineWidth(2);

        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

        BlockPos base = new BlockPos(0, 0, 0);

        Shape shape = ShapeCardItem.getShape(stack);
        BlockPos dimension = ShapeCardItem.getDimension(stack);
        BlockPos offset = new BlockPos(0, 128, 0);
        BlockPos clamped = new BlockPos(Math.min(dimension.getX(), 512), Math.min(dimension.getY(), 256), Math.min(dimension.getZ(), 512));
        ShapeCardItem.composeShape(stack, shape, null, new BlockPos(0, 0, 0), clamped, offset, new AbstractCollection<BlockPos>() {
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
                mcjty.lib.gui.RenderHelper.renderHighLightedBlocksOutline(buffer,
                        base.getX() + coordinate.getX(), base.getY() + coordinate.getY(), base.getZ() + coordinate.getZ(),
                        .4f, .3f, .5f, 1.0f);
                return true;
            }

            @Override
            public int size() {
                return 0;
            }
        }, ShapeCardItem.MAXIMUM_COUNT+1, false, null);

        tessellator.draw();
        GlStateManager.popMatrix();

        GlStateManager.enableTexture2D();
        RenderHelper.enableGUIStandardItemLighting();
    }
}
