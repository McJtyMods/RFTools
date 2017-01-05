package mcjty.rftools.blocks.screens.modulesclient;

import mcjty.rftools.api.screens.IClientScreenModule;
import mcjty.rftools.api.screens.IModuleGuiBuilder;
import mcjty.rftools.api.screens.IModuleRenderHelper;
import mcjty.rftools.api.screens.ModuleRenderInfo;
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
    private int align = 0;  // 0 == left, 1 == center, 2 == right

    private boolean dirty = true;
    private int labelx;
    private String labelLine;

    @Override
    public TransformMode getTransformMode() {
        return TransformMode.TEXT;
    }

    private void setup(FontRenderer fontRenderer) {
        if (!dirty) {
            return;
        }
        dirty = false;

        if (!line.isEmpty()) {
            int w = 36;
            labelx = 7;
            labelLine = fontRenderer.trimStringToWidth(line, w);
            switch (align) {
                case 0:
                    break;
                case 1:
                    labelx += (w - fontRenderer.getStringWidth(labelLine)) / 2;
                    break;
                case 2:
                    labelx += w - fontRenderer.getStringWidth(labelLine);
                    break;
            }
        }
    }

    @Override
    public int getHeight() {
        return 10;
    }

    @Override
    public void render(IModuleRenderHelper renderHelper, FontRenderer fontRenderer, int currenty, IModuleDataBoolean screenData, ModuleRenderInfo renderInfo) {
        GlStateManager.disableLighting();
        setup(fontRenderer);

        int xoffset;
        if (!line.isEmpty()) {
            fontRenderer.drawString(labelLine, labelx, currenty, color);
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
        guiBuilder
                .label("Label:").text("text", "Label text").color("color", "Color for the label").nl()
                .label("Yes:").text("yestext", "Positive text").color("yescolor", "Color for the positive text").nl()
                .label("No:").text("notext", "Negative text").color("nocolor", "Color for the negative text").nl()
                .choices("align", "Label alignment", "Left", "Center", "Right").nl()
                .label("Block:").block("monitor").nl();
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
            if (tagCompound.hasKey("align")) {
                String alignment = tagCompound.getString("align");
                align = "Left".equals(alignment) ? 0 : ("Right".equals(alignment) ? 2 : 1);
            } else {
                align = 0;
            }
            dirty = true;
        }
    }

    @Override
    public boolean needsServerData() {
        return true;
    }
}
