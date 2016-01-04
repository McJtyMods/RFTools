package mcjty.rftools.blocks.screens.modulesclient;

import mcjty.lib.gui.RenderHelper;
import mcjty.lib.gui.widgets.Panel;
import mcjty.rftools.blocks.screens.ModuleGuiChanged;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

public class ButtonClientScreenModule implements ClientScreenModule {
    private String line = "";
    private String button = "";
    private boolean toggle = false;
    private int color = 0xffffff;
    private int buttonColor = 0xffffff;
    private boolean activated = false;

    @Override
    public TransformMode getTransformMode() {
        return TransformMode.TEXT;
    }

    @Override
    public int getHeight() {
        return 14;
    }

    @Override
    public void render(FontRenderer fontRenderer, int currenty, Object[] screenData, float factor) {
        GL11.glDisable(GL11.GL_LIGHTING);
        int xoffset;
        if (!line.isEmpty()) {
            fontRenderer.drawString(line, 7, currenty + 2, color);
            xoffset = 7 + 80;
        } else {
            xoffset = 7 + 5;
        }

        boolean act = false;
        if (toggle) {
            if (screenData != null && screenData.length >= 1 && screenData[0] instanceof Integer) {
                act = ((Integer) screenData[0]) > 0;
            }
        } else {
            act = activated;
        }

        RenderHelper.drawBeveledBox(xoffset - 5, currenty, 130 - 7, currenty + 12, act ? 0xff333333 : 0xffeeeeee, act ? 0xffeeeeee : 0xff333333, 0xff666666);
        fontRenderer.drawString(fontRenderer.trimStringToWidth(button, 130 - 7 - xoffset), xoffset + (act ? 1 : 0), currenty + 2 + (act ? 1 : 0), buttonColor);
    }

    @Override
    public void mouseClick(World world, int x, int y, boolean clicked) {
        int xoffset;
        if (!line.isEmpty()) {
            xoffset = 80;
        } else {
            xoffset = 5;
        }
        activated = false;
        if (x >= xoffset) {
            activated = clicked;
        }
    }

    @Override
    public Panel createGui(Minecraft mc, Gui gui, final NBTTagCompound currentData, final ModuleGuiChanged moduleGuiChanged) {
        return new ScreenModuleGuiBuilder(mc, gui, currentData, moduleGuiChanged).
                label("Label:").text("text", "Label text").color("color", "Label color").nl().
                label("Button:").text("button", "Button text").color("buttonColor", "Button color").nl().
                toggle("toggle", "Toggle", "Toggle button mode").nl().
                build();
    }

    @Override
    public void setupFromNBT(NBTTagCompound tagCompound, int dim, int x, int y, int z) {
        if (tagCompound != null) {
            line = tagCompound.getString("text");
            button = tagCompound.getString("button");
            if (tagCompound.hasKey("color")) {
                color = tagCompound.getInteger("color");
            } else {
                color = 0xffffff;
            }
            if (tagCompound.hasKey("buttonColor")) {
                buttonColor = tagCompound.getInteger("buttonColor");
            } else {
                buttonColor = 0xffffff;
            }
            toggle = tagCompound.getBoolean("toggle");
        }
    }

    @Override
    public boolean needsServerData() {
        return true;
    }
}
