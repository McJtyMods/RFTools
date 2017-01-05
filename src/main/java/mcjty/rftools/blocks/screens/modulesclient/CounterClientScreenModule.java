package mcjty.rftools.blocks.screens.modulesclient;

import mcjty.lib.varia.BlockPosTools;
import mcjty.rftools.api.screens.*;
import mcjty.rftools.api.screens.data.IModuleDataInteger;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CounterClientScreenModule implements IClientScreenModule<IModuleDataInteger> {

    private String line = "";
    private int color = 0xffffff;
    private int cntcolor = 0xffffff;
    protected int dim = 0;
    private FormatStyle format = FormatStyle.MODE_FULL;
    protected BlockPos coordinate = BlockPosTools.INVALID;
    private int align = 0;  // 0 == left, 1 == center, 2 == right

    private boolean dirty = true;
    private int labelx;
    private String labelLine;

    @Override
    public TransformMode getTransformMode() {
        return TransformMode.TEXT;
    }

    private void setup(FontRenderer fontRenderer) {
        if (!dirty) {
            return;
        }
        dirty = false;

        if (!line.isEmpty()) {
            int w = 36;
            labelx = 7;
            labelLine = fontRenderer.trimStringToWidth(line, w);
            switch (align) {
                case 0:
                    break;
                case 1:
                    labelx += (w - fontRenderer.getStringWidth(labelLine)) / 2;
                    break;
                case 2:
                    labelx += w - fontRenderer.getStringWidth(labelLine);
                    break;
            }
        }
    }


    @Override
    public int getHeight() {
        return 10;
    }

    @Override
    public void render(IModuleRenderHelper renderHelper, FontRenderer fontRenderer, int currenty, IModuleDataInteger screenData, ModuleRenderInfo renderInfo) {
        GlStateManager.disableLighting();
        setup(fontRenderer);

        int xoffset;
        if (!line.isEmpty()) {
            fontRenderer.drawString(labelLine, labelx, currenty, color);
            xoffset = 7 + 40;
        } else {
            xoffset = 7;
        }

        if (!BlockPosTools.INVALID.equals(coordinate)) {
            int counter;
            if (screenData != null) {
                counter = screenData.get();
            } else {
                counter = 0;
            }
            fontRenderer.drawString(renderHelper.format(String.valueOf(counter), format), xoffset, currenty, cntcolor);
        } else {
            fontRenderer.drawString("<invalid>", xoffset, currenty, 0xff0000);
        }
    }

    @Override
    public void mouseClick(World world, int x, int y, boolean clicked) {

    }

    @Override
    public void createGui(IModuleGuiBuilder guiBuilder) {
        guiBuilder
                .label("Label:").text("text", "Label text").nl()
                .label("L:").color("color", "Color for the label").label("C:").color("cntcolor", "Color for the counter").nl()
                .format("format")
                .choices("align", "Label alignment", "Left", "Center", "Right").nl()
                .label("Block:").block("monitor").nl();
    }

    @Override
    public void setupFromNBT(NBTTagCompound tagCompound, int dim, BlockPos pos) {
        if (tagCompound != null) {
            line = tagCompound.getString("text");
            if (tagCompound.hasKey("color")) {
                color = tagCompound.getInteger("color");
            } else {
                color = 0xffffff;
            }
            if (tagCompound.hasKey("cntcolor")) {
                cntcolor = tagCompound.getInteger("cntcolor");
            } else {
                cntcolor = 0xffffff;
            }
            if (tagCompound.hasKey("align")) {
                String alignment = tagCompound.getString("align");
                align = "Left".equals(alignment) ? 0 : ("Right".equals(alignment) ? 2 : 1);
            } else {
                align = 0;
            }
            dirty = true;

            format = FormatStyle.values()[tagCompound.getInteger("format")];

            setupCoordinateFromNBT(tagCompound, dim, pos);
        }
    }

    protected void setupCoordinateFromNBT(NBTTagCompound tagCompound, int dim, BlockPos pos) {
        coordinate = BlockPosTools.INVALID;
        if (tagCompound.hasKey("monitorx")) {
            if (tagCompound.hasKey("monitordim")) {
                this.dim = tagCompound.getInteger("monitordim");
            } else {
                // Compatibility reasons
                this.dim = tagCompound.getInteger("dim");
            }
            if (dim == this.dim) {
                BlockPos c = new BlockPos(tagCompound.getInteger("monitorx"), tagCompound.getInteger("monitory"), tagCompound.getInteger("monitorz"));
                int dx = Math.abs(c.getX() - pos.getX());
                int dy = Math.abs(c.getY() - pos.getY());
                int dz = Math.abs(c.getZ() - pos.getZ());
                if (dx <= 64 && dy <= 64 && dz <= 64) {
                    coordinate = c;
                }
            }
        }
    }

    @Override
    public boolean needsServerData() {
        return true;
    }
}
