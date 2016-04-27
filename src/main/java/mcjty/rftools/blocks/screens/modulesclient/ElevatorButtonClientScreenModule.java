package mcjty.rftools.blocks.screens.modulesclient;

import mcjty.lib.gui.RenderHelper;
import mcjty.rftools.api.screens.IClientScreenModule;
import mcjty.rftools.api.screens.IModuleGuiBuilder;
import mcjty.rftools.api.screens.IModuleRenderHelper;
import mcjty.rftools.api.screens.ModuleRenderInfo;
import mcjty.rftools.api.screens.data.IModuleDataContents;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ElevatorButtonClientScreenModule implements IClientScreenModule<IModuleDataContents> {

    public static final int LARGESIZE = 22;
    public static final int SMALLSIZE = 16;

    private int buttonColor = 0xffffff;
    private int currentLevelButtonColor = 0xffff00;
    private boolean vertical = false;
    private boolean large = false;
    private boolean lights = false;
    private boolean start1 = false;

    @Override
    public TransformMode getTransformMode() {
        return TransformMode.TEXT;
    }

    @Override
    public int getHeight() {
        if (vertical) {
            return large ? (LARGESIZE*5) : (SMALLSIZE *7);
        } else {
            return large ? LARGESIZE : SMALLSIZE;
        }
    }

    private int getDimension() {
        return large ? LARGESIZE : SMALLSIZE;
    }

    @Override
    public void render(IModuleRenderHelper renderHelper, FontRenderer fontRenderer, int currenty, IModuleDataContents screenData, ModuleRenderInfo renderInfo) {
        GlStateManager.disableLighting();

        if (screenData == null) {
            return;
        }
        int currentLevel = (int) screenData.getContents();
        int buttons = (int) screenData.getMaxContents();
        int xoffset = 5;
        for (int i = 0; i < buttons; i++) {
            int level = vertical ? buttons-i-1 : i;
            String test = String.valueOf(level + (start1 ? 1 : 0));
            int col = level == currentLevel ? this.currentLevelButtonColor : this.buttonColor;
            int textoffset = large ? 3 : 0;
            if (vertical) {
                if (lights) {
                    RenderHelper.drawBeveledBox(xoffset, currenty, xoffset + 120, currenty + getDimension() - 2, 0xffffffff, 0xffffffff, 0xff000000 + col);
                } else {
                    RenderHelper.drawBeveledBox(xoffset, currenty, xoffset + 120, currenty + getDimension() - 2, 0xffeeeeee, 0xff333333, 0xff666666);
                    fontRenderer.drawString(fontRenderer.trimStringToWidth(test, 120), xoffset + 3 + textoffset, currenty + 2 + textoffset, col);
                }
                currenty += getDimension() - 2;
            } else {
                if (lights) {
                    RenderHelper.drawBeveledBox(xoffset, currenty, xoffset + getDimension() - 4, currenty + getDimension() - 2, 0xffffffff, 0xffffffff, 0xff000000 + col);
                } else {
                    RenderHelper.drawBeveledBox(xoffset, currenty, xoffset + getDimension() - 4, currenty + getDimension() - 2, 0xffeeeeee, 0xff333333, 0xff666666);
                    fontRenderer.drawString(fontRenderer.trimStringToWidth(test, getDimension() - 4), xoffset + 3 + textoffset, currenty + 2 + textoffset, col);
                }
                xoffset += getDimension() - 2;
            }
        }
    }

    @Override
    public void mouseClick(World world, int x, int y, boolean clicked) {
    }

    @Override
    public void createGui(IModuleGuiBuilder guiBuilder) {
        guiBuilder
                .color("buttonColor", "Button color").color("curColor", "Current level button color").nl()
                .toggle("vertical", "Vertical", "Order the buttons vertically").toggle("large", "Large", "Larger buttons").nl()
                .toggle("lights", "Lights", "Use buttons resembling lights").toggle("start1", "Start 1", "start numbering at 1 instead of 0").nl()
                .label("Block:").block("elevator").nl();
    }

    @Override
    public void setupFromNBT(NBTTagCompound tagCompound, int dim, BlockPos pos) {
        if (tagCompound != null) {
            if (tagCompound.hasKey("buttonColor")) {
                buttonColor = tagCompound.getInteger("buttonColor");
            } else {
                buttonColor = 0xffffff;
            }
            if (tagCompound.hasKey("curColor")) {
                currentLevelButtonColor = tagCompound.getInteger("curColor");
            } else {
                currentLevelButtonColor = 0xffff00;
            }
            vertical = tagCompound.getBoolean("vertical");
            large = tagCompound.getBoolean("large");
            lights = tagCompound.getBoolean("lights");
            start1 = tagCompound.getBoolean("start1");
        }
    }

    @Override
    public boolean needsServerData() {
        return true;
    }
}
