package mcjty.rftools.blocks.screens.modulesclient;

import mcjty.lib.varia.BlockPosTools;
import mcjty.rftools.api.screens.*;
import mcjty.rftools.api.screens.data.IModuleDataContents;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EnergyBarClientScreenModule implements IClientScreenModule<IModuleDataContents> {

    private String line = "";
    private int color = 0xffffff;
    private int rfcolor = 0xffffff;
    private int rfcolorNeg = 0xffffff;
    protected int dim = 0;
    private boolean hidebar = false;
    private boolean hidetext = false;
    private boolean showdiff = false;
    private boolean showpct = false;
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
    public void render(IModuleRenderHelper renderHelper, FontRenderer fontRenderer, int currenty, IModuleDataContents screenData, ModuleRenderInfo renderInfo) {
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
            renderHelper.renderLevel(fontRenderer, xoffset, currenty, screenData, "RF", hidebar, hidetext, showpct, showdiff, rfcolor, rfcolorNeg, 0xffff0000, 0xff333300, format);
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
                .label("Label:").text("text", "Label text").color("color", "Color for the label").nl()
                .label("RF+:").color("rfcolor", "Color for the RF text").label("RF-:").color("rfcolor_neg", "Color for the negative", "RF/tick ratio").nl()
                .toggleNegative("hidebar", "Bar", "Toggle visibility of the", "energy bar").mode("RF").format("format").nl()
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
            if (tagCompound.hasKey("rfcolor")) {
                rfcolor = tagCompound.getInteger("rfcolor");
            } else {
                rfcolor = 0xffffff;
            }
            if (tagCompound.hasKey("rfcolor_neg")) {
                rfcolorNeg = tagCompound.getInteger("rfcolor_neg");
            } else {
                rfcolorNeg = 0xffffff;
            }
            if (tagCompound.hasKey("align")) {
                String alignment = tagCompound.getString("align");
                align = "Left".equals(alignment) ? 0 : ("Right".equals(alignment) ? 2 : 1);
            } else {
                align = 0;
            }
            dirty = true;

            hidebar = tagCompound.getBoolean("hidebar");
            hidetext = tagCompound.getBoolean("hidetext");
            showdiff = tagCompound.getBoolean("showdiff");
            showpct = tagCompound.getBoolean("showpct");
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
