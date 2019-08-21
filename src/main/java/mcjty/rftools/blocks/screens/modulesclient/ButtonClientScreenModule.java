package mcjty.rftools.blocks.screens.modulesclient;

import com.mojang.blaze3d.platform.GlStateManager;
import mcjty.lib.client.RenderHelper;
import mcjty.rftools.api.screens.*;
import mcjty.rftools.api.screens.data.IModuleDataBoolean;
import mcjty.rftools.blocks.screens.modulesclient.helper.ScreenTextHelper;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ButtonClientScreenModule implements IClientScreenModule<IModuleDataBoolean> {
    private String line = "";
    private String button = "";
    private boolean toggle = false;
    private int color = 0xffffff;
    private int buttonColor = 0xffffff;
    private boolean activated = false;

    private ITextRenderHelper labelCache = new ScreenTextHelper();
    private ITextRenderHelper buttonCache = new ScreenTextHelper();

    @Override
    public TransformMode getTransformMode() {
        return TransformMode.TEXT;
    }

    @Override
    public int getHeight() {
        return 14;
    }

    @Override
    public void render(IModuleRenderHelper renderHelper, FontRenderer fontRenderer, int currenty, IModuleDataBoolean screenData, ModuleRenderInfo renderInfo) {
        GlStateManager.disableLighting();
        GlStateManager.enableDepth();
        GlStateManager.depthMask(false);

        int xoffset;
        int buttonWidth;
        if (!line.isEmpty()) {
            labelCache.setup(line, 316, renderInfo);
            labelCache.renderText(0, currenty + 2, color, renderInfo);
            xoffset = 7 + 80;
            buttonWidth = 170;
        } else {
            xoffset = 7 + 5;
            buttonWidth = 490;
        }

        boolean act = false;
        if (toggle) {
            if (screenData != null) {
                act = screenData.get();
            }
        } else {
            act = activated;
        }

        RenderHelper.drawBeveledBox(xoffset - 5, currenty, 130 - 7, currenty + 12, act ? 0xff333333 : 0xffeeeeee, act ? 0xffeeeeee : 0xff333333, 0xff666666);
        buttonCache.setup(button, buttonWidth, renderInfo);
        buttonCache.renderText(xoffset -10 + (act ? 1 : 0), currenty + 2, buttonColor, renderInfo);
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
    public void setupFromNBT(CompoundNBT tagCompound, int dim, BlockPos pos) {
        if (tagCompound != null) {
            line = tagCompound.getString("text");
            button = tagCompound.getString("button");
            if (tagCompound.hasKey("color")) {
                color = tagCompound.getInt("color");
            } else {
                color = 0xffffff;
            }
            if (tagCompound.hasKey("buttonColor")) {
                buttonColor = tagCompound.getInt("buttonColor");
            } else {
                buttonColor = 0xffffff;
            }
            toggle = tagCompound.getBoolean("toggle");
            if (tagCompound.hasKey("align")) {
                String alignment = tagCompound.getString("align");
                labelCache.align(TextAlign.get(alignment));
            } else {
                labelCache.align(TextAlign.ALIGN_LEFT);
            }
            buttonCache.setDirty();
        }
    }

    @Override
    public boolean needsServerData() {
        return true;
    }
}
