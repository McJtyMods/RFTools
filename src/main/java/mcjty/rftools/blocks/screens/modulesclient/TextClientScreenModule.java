package mcjty.rftools.blocks.screens.modulesclient;

import mcjty.rftools.api.screens.*;
import mcjty.rftools.api.screens.data.IModuleData;
import mcjty.rftools.blocks.screens.modulesclient.helper.ScreenTextHelper;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TextClientScreenModule implements IClientScreenModule<IModuleData> {
    private String line = "";
    private int color = 0xffffff;

    private ITextRenderHelper cache = new ScreenTextHelper();

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
        cache.setup(line, 512, renderInfo);
        int y = cache.isLarge() ? (currenty / 2 + 1) : currenty;
        cache.renderText(0, y, color, renderInfo);
    }

    @Override
    public void mouseClick(World world, int x, int y, boolean clicked) {

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
        cache.large(large);
        cache.setDirty();
    }

    @Override
    public void setupFromNBT(CompoundNBT tagCompound, int dim, BlockPos pos) {
        if (tagCompound != null) {
            line = tagCompound.getString("text");
            if (tagCompound.hasKey("color")) {
                color = tagCompound.getInteger("color");
            } else {
                color = 0xffffff;
            }
            cache.large(tagCompound.getBoolean("large"));
            if (tagCompound.hasKey("align")) {
                String alignment = tagCompound.getString("align");
                cache.align(TextAlign.get(alignment));
            } else {
                cache.align(TextAlign.ALIGN_LEFT);
            }
        }
    }

    @Override
    public boolean needsServerData() {
        return false;
    }
}
