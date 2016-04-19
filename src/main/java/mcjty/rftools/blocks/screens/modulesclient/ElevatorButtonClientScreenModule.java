package mcjty.rftools.blocks.screens.modulesclient;

import mcjty.lib.gui.RenderHelper;
import mcjty.rftools.api.screens.IClientScreenModule;
import mcjty.rftools.api.screens.IModuleGuiBuilder;
import mcjty.rftools.api.screens.IModuleRenderHelper;
import mcjty.rftools.api.screens.data.IModuleDataContents;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ElevatorButtonClientScreenModule implements IClientScreenModule<IModuleDataContents> {
    private int buttonColor = 0xffffff;
    private int currentLevelButtonColor = 0xffff00;

    @Override
    public TransformMode getTransformMode() {
        return TransformMode.TEXT;
    }

    @Override
    public int getHeight() {
        return 16;
    }

    @Override
    public void render(IModuleRenderHelper renderHelper, FontRenderer fontRenderer, int currenty, IModuleDataContents screenData, float factor) {
        GlStateManager.disableLighting();
        int xoffset = 5;

        if (screenData == null) {
            return;
        }
        int currentLevel = (int) screenData.getContents();
        int buttons = (int) screenData.getMaxContents();
        for (int i = 0 ; i < buttons ; i++) {
            RenderHelper.drawBeveledBox(xoffset, currenty, xoffset + 12, currenty + 14, 0xffeeeeee, 0xff333333, 0xff666666);
            int col = i == currentLevel ? this.currentLevelButtonColor : this.buttonColor;
            fontRenderer.drawString(fontRenderer.trimStringToWidth(String.valueOf(i), 12), xoffset+3, currenty + 2, col);
            xoffset += 14;
        }
    }

    @Override
    public void mouseClick(World world, int x, int y, boolean clicked) {
    }

    @Override
    public void createGui(IModuleGuiBuilder guiBuilder) {
        guiBuilder
                .color("buttonColor", "Button color").color("curColor", "Current level button color").nl()
                .label("Block:").block("elevator").nl();
    }

    @Override
    public void setupFromNBT(NBTTagCompound tagCompound, int dim, BlockPos pos) {
        if (tagCompound != null) {
            if (tagCompound.hasKey("buttonColor")) {
                buttonColor = tagCompound.getInteger("buttonColor");
            } else if (tagCompound.hasKey("curColor")) {
                currentLevelButtonColor = tagCompound.getInteger("curColor");
            } else {
                buttonColor = 0xffffff;
            }
        }
    }

    @Override
    public boolean needsServerData() {
        return true;
    }
}
