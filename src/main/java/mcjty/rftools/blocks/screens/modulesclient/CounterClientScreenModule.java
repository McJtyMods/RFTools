package mcjty.rftools.blocks.screens.modulesclient;

import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.varia.BlockPosTools;
import mcjty.rftools.blocks.screens.ModuleGuiChanged;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class CounterClientScreenModule implements ClientScreenModule {

    private String line = "";
    private int color = 0xffffff;
    private int cntcolor = 0xffffff;
    protected int dim = 0;
    private FormatStyle format = FormatStyle.MODE_FULL;
    protected BlockPos coordinate = BlockPosTools.INVALID;

    @Override
    public TransformMode getTransformMode() {
        return TransformMode.TEXT;
    }

    @Override
    public int getHeight() {
        return 10;
    }

    @Override
    public void render(FontRenderer fontRenderer, int currenty, Object[] screenData, float factor) {
        GlStateManager.disableLighting();
        int xoffset;
        if (!line.isEmpty()) {
            fontRenderer.drawString(line, 7, currenty, color);
            xoffset = 7 + 40;
        } else {
            xoffset = 7;
        }

        if (!BlockPosTools.INVALID.equals(coordinate)) {
            int counter;
            if (screenData != null && screenData.length > 0 && screenData[0] instanceof Integer) {
                counter = (Integer) screenData[0];
            } else {
                counter = 0;
            }
            fontRenderer.drawString(ClientScreenModuleHelper.format(String.valueOf(counter), format), xoffset, currenty, cntcolor);
        } else {
            fontRenderer.drawString("<invalid>", xoffset, currenty, 0xff0000);
        }
    }

    @Override
    public void mouseClick(World world, int x, int y, boolean clicked) {

    }

    @Override
    public Panel createGui(Minecraft mc, Gui gui, final NBTTagCompound currentData, final ModuleGuiChanged moduleGuiChanged) {
        return new ScreenModuleGuiBuilder(mc, gui, currentData, moduleGuiChanged).
                label("Label:").text("text", "Label text").nl().
                label("L:").color("color", "Color for the label").label("C:").color("cntcolor", "Color for the counter").nl().
                format().nl().
                label("Block:").monitor().nl().
                build();
    }

    @Override
    public void setupFromNBT(NBTTagCompound tagCompound, int dim, int x, int y, int z) {
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

            format = FormatStyle.values()[tagCompound.getInteger("format")];

            setupCoordinateFromNBT(tagCompound, dim, x, y, z);
        }
    }

    protected void setupCoordinateFromNBT(NBTTagCompound tagCompound, int dim, int x, int y, int z) {
        coordinate = BlockPosTools.INVALID;
        if (tagCompound.hasKey("monitorx")) {
            this.dim = tagCompound.getInteger("dim");
            if (dim == this.dim) {
                BlockPos c = new BlockPos(tagCompound.getInteger("monitorx"), tagCompound.getInteger("monitory"), tagCompound.getInteger("monitorz"));
                int dx = Math.abs(c.getX() - x);
                int dy = Math.abs(c.getY() - y);
                int dz = Math.abs(c.getZ() - z);
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
