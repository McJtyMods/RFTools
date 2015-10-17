package mcjty.rftools.blocks.screens.modulesclient;

import mcjty.lib.gui.widgets.Panel;
import mcjty.rftools.blocks.screens.ModuleGuiChanged;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

public class TextClientScreenModule implements ClientScreenModule {
    private String line = "";
    private int color = 0xffffff;
    private boolean large = false;

    @Override
    public TransformMode getTransformMode() {
        return large ? TransformMode.TEXTLARGE : TransformMode.TEXT;
    }

    @Override
    public int getHeight() {
        return large ? 20 : 10;
    }

    @Override
    public void render(FontRenderer fontRenderer, int currenty, Object[] screenData, float factor) {
        GL11.glDisable(GL11.GL_LIGHTING);
        if (large) {
            fontRenderer.drawString(fontRenderer.trimStringToWidth(line, 60), 4, currenty / 2 + 1, color);
        } else {
            fontRenderer.drawString(fontRenderer.trimStringToWidth(line, 115), 7, currenty, color);
        }
    }

    @Override
    public void mouseClick(World world, int x, int y, boolean clicked) {

    }

    @Override
    public Panel createGui(Minecraft mc, Gui gui, final NBTTagCompound currentData, final ModuleGuiChanged moduleGuiChanged) {
        return new ScreenModuleGuiBuilder(mc, gui, currentData, moduleGuiChanged).
                label("Text:").text("text", "Text to show").color("color", "Color for the text").nl().
                toggle("large", "Large", "Large or small font").nl().
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
            large = tagCompound.getBoolean("large");
        }
    }

    @Override
    public boolean needsServerData() {
        return false;
    }
}
