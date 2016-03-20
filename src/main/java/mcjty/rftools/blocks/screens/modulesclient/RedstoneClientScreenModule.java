package mcjty.rftools.blocks.screens.modulesclient;

import mcjty.rftools.api.screens.IClientScreenModule;
import mcjty.rftools.api.screens.IModuleGuiBuilder;
import mcjty.rftools.api.screens.IModuleRenderHelper;
import mcjty.rftools.api.screens.data.IModuleDataBoolean;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RedstoneClientScreenModule implements IClientScreenModule<IModuleDataBoolean> {

    private String line = "";
    private String yestext = "on";
    private String notext = "off";
    private int color = 0xffffff;
    private int yescolor = 0xffffff;
    private int nocolor = 0xffffff;
    private int dim = 0;

    @Override
    public TransformMode getTransformMode() {
        return TransformMode.TEXT;
    }

    @Override
    public int getHeight() {
        return 10;
    }

    @Override
    public void render(IModuleRenderHelper renderHelper, FontRenderer fontRenderer, int currenty, IModuleDataBoolean screenData, float factor) {
        GlStateManager.disableLighting();
        int xoffset;
        if (!line.isEmpty()) {
            fontRenderer.drawString(line, 7, currenty, color);
            xoffset = 7 + 40;
        } else {
            xoffset = 7;
        }

        if (screenData != null) {
            boolean rs = screenData.get();
            fontRenderer.drawString(rs ? yestext : notext, xoffset, currenty, rs ? yescolor : nocolor);
        } else {
            fontRenderer.drawString("<invalid>", xoffset, currenty, 0xff0000);
        }
    }

    @Override
    public void mouseClick(World world, int x, int y, boolean clicked) {

    }

    @Override
    public void createGui(IModuleGuiBuilder guiBuilder) {
        guiBuilder.
                label("Label:").text("text", "Label text").color("color", "Color for the label").nl().
                label("Yes:").text("yestext", "Positive text").color("yescolor", "Color for the positive text").nl().
                label("No:").text("notext", "Negative text").color("nocolor", "Color for the negative text").nl().
                label("Block:").block("monitor").nl();
    }

    @Override
    public void setupFromNBT(NBTTagCompound tagCompound, int dim, BlockPos pos) {
        if (tagCompound != null) {
            line = tagCompound.getString("text");
            if (tagCompound.hasKey("yestext")) {
                yestext = tagCompound.getString("yestext");
            }
            if (tagCompound.hasKey("notext")) {
                notext = tagCompound.getString("notext");
            }
            if (tagCompound.hasKey("color")) {
                color = tagCompound.getInteger("color");
            } else {
                color = 0xffffff;
            }
            if (tagCompound.hasKey("yescolor")) {
                yescolor = tagCompound.getInteger("yescolor");
            } else {
                yescolor = 0xffffff;
            }
            if (tagCompound.hasKey("nocolor")) {
                nocolor = tagCompound.getInteger("nocolor");
            } else {
                nocolor = 0xffffff;
            }
        }
    }

    @Override
    public boolean needsServerData() {
        return true;
    }
}
