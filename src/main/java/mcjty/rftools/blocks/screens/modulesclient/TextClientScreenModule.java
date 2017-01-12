package mcjty.rftools.blocks.screens.modulesclient;

import mcjty.rftools.api.screens.IClientScreenModule;
import mcjty.rftools.api.screens.IModuleGuiBuilder;
import mcjty.rftools.api.screens.IModuleRenderHelper;
import mcjty.rftools.api.screens.ModuleRenderInfo;
import mcjty.rftools.api.screens.data.IModuleData;
import mcjty.rftools.blocks.screens.ScreenConfiguration;
import mcjty.rftools.proxy.ClientProxy;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TextClientScreenModule implements IClientScreenModule {
    private String line = "";
    private int color = 0xffffff;

    private ScreenTextCache cache = new ScreenTextCache();

    @Override
    public TransformMode getTransformMode() {
        return cache.isLarge() ? TransformMode.TEXTLARGE : TransformMode.TEXT;
    }

    @Override
    public int getHeight() {
        return cache.isLarge() ? 20 : 10;
    }

    @Override
    public void render(IModuleRenderHelper renderHelper, FontRenderer fontRenderer, int currenty, IModuleData screenData, ModuleRenderInfo renderInfo) {
        GlStateManager.disableLighting();
        cache.setup(fontRenderer, line);
        int y = cache.isLarge() ? (currenty / 2 + 1) : currenty;

        if (ScreenConfiguration.useTruetype) {
            float r = (color >> 16 & 255) / 255.0f;
            float g = (color >> 8 & 255) / 255.0f;
            float b = (color & 255) / 255.0f;
            ClientProxy.font.drawString(cache.getTextx(), 128 - y, cache.getText(), 0.25f, 0.25f, -512f-40f, r, g, b, 1.0f);
        } else {
            fontRenderer.drawString(cache.getText(), cache.getTextx(), y, color);
        }
    }

    @Override
    public void mouseClick(World world, int x, int y, boolean clicked) {

    }

    @Override
    public void createGui(IModuleGuiBuilder guiBuilder) {
        guiBuilder
                .label("Text:").text("text", "Text to show").color("color", "Color for the text").nl()
                .toggle("large", "Large", "Large or small font")
                .choices("align", "Text alignment", "Left", "Center", "Right").nl();

    }

    public void setLine(String line) {
        this.line = line;
        cache.setDirty();
    }

    public void setColor(int color) {
        this.color = color;
        cache.setDirty();
    }

    public void setLarge(boolean large) {
        cache.setLarge(large);
        cache.setDirty();
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
            cache.setLarge(tagCompound.getBoolean("large"));
            if (tagCompound.hasKey("align")) {
                String alignment = tagCompound.getString("align");
                cache.setAlign("Left".equals(alignment) ? 0 : ("Right".equals(alignment) ? 2 : 1));
            } else {
                cache.setAlign(0);
            }
            cache.setDirty();
        }
    }

    @Override
    public boolean needsServerData() {
        return false;
    }
}
