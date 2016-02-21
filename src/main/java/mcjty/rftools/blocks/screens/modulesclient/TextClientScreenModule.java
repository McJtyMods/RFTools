package mcjty.rftools.blocks.screens.modulesclient;

import mcjty.rftools.api.screens.*;
import mcjty.rftools.api.screens.data.IModuleData;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class TextClientScreenModule implements IClientScreenModule {
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
    public void render(IModuleRenderHelper renderHelper, FontRenderer fontRenderer, int currenty, IModuleData screenData, float factor) {
        GlStateManager.disableLighting();
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
    public void createGui(IModuleGuiBuilder guiBuilder) {
        guiBuilder.
                label("Text:").text("text", "Text to show").color("color", "Color for the text").nl().
                toggle("large", "Large", "Large or small font").nl();
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
