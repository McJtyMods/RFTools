package mcjty.rftools.blocks.screens.modulesclient;

import mcjty.gui.widgets.Panel;
import mcjty.rftools.blocks.screens.ModuleGuiChanged;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

public class DimensionClientScreenModule implements ClientScreenModule {
    private String line = "";
    private int color = 0xffffff;
    private int rfcolor = 0xffffff;
    private int rfcolorNeg = 0xffffff;
    private boolean hidebar = false;
    private boolean hidetext = false;
    private boolean showdiff = false;
    private boolean showpct = false;
    private FormatStyle format = FormatStyle.MODE_FULL;

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
        GL11.glDisable(GL11.GL_LIGHTING);
        int xoffset;
        if (!line.isEmpty()) {
            fontRenderer.drawString(line, 7, currenty, color);
            xoffset = 7 + 40;
        } else {
            xoffset = 7;
        }

        ClientScreenModuleHelper.renderLevel(fontRenderer, xoffset, currenty, screenData, "RF", hidebar, hidetext, showpct, showdiff, rfcolor, rfcolorNeg, 0xffff0000, 0xff333300, format);
    }

    @Override
    public void mouseClick(World world, int x, int y, boolean clicked) {

    }

    @Override
    public Panel createGui(Minecraft mc, Gui gui, final NBTTagCompound currentData, final ModuleGuiChanged moduleGuiChanged) {
        return new ScreenModuleGuiBuilder(mc, gui, currentData, moduleGuiChanged).
                label("Label:").text("text", "Label text").color("color", "Color for the label").nl().
                label("RF+:").color("rfcolor", "Color for the RF text").label("RF-:").color("rfcolor_neg", "Color for the negative", "RF/tick ratio").nl().
                toggleNegative("hidebar", "Bar", "Toggle visibility of the", "energy bar").mode("RF").format().nl().
                label("Dimension:").integer("dim", "The id of the dimension", "to monitor").nl().
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

            hidebar = tagCompound.getBoolean("hidebar");
            hidetext = tagCompound.getBoolean("hidetext");
            showdiff = tagCompound.getBoolean("showdiff");
            showpct = tagCompound.getBoolean("showpct");

            format = FormatStyle.values()[tagCompound.getInteger("format")];
        }
    }

    @Override
    public boolean needsServerData() {
        return true;
    }
}
