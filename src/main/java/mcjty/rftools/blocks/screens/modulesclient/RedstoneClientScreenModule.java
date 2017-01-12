package mcjty.rftools.blocks.screens.modulesclient;

import mcjty.rftools.api.screens.IClientScreenModule;
import mcjty.rftools.api.screens.IModuleGuiBuilder;
import mcjty.rftools.api.screens.IModuleRenderHelper;
import mcjty.rftools.api.screens.ModuleRenderInfo;
import mcjty.rftools.api.screens.data.IModuleDataBoolean;
import mcjty.rftools.blocks.screens.ScreenConfiguration;
import mcjty.rftools.proxy.ClientProxy;
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

    private ScreenTextCache labelCache = new ScreenTextCache();

    @Override
    public TransformMode getTransformMode() {
        return TransformMode.TEXT;
    }

    @Override
    public int getHeight() {
        return 10;
    }

    @Override
    public void render(IModuleRenderHelper renderHelper, FontRenderer fontRenderer, int currenty, IModuleDataBoolean screenData, ModuleRenderInfo renderInfo) {
        GlStateManager.disableLighting();

        int xoffset;
        if (!line.isEmpty()) {
            labelCache.setup(fontRenderer, line, 160);
            labelCache.renderText(fontRenderer, color, 0, currenty);
            xoffset = 7 + 40;
        } else {
            xoffset = 7;
        }

        String text;
        int col;
        if (screenData != null) {
            boolean rs = screenData.get();
            text = rs ? yestext : notext;
            col = rs ? yescolor : nocolor;
        } else {
            text = "<invalid>";
            col = 0xff0000;
        }
        if (ScreenConfiguration.useTruetype) {
            float r = (col >> 16 & 255) / 255.0f;
            float g = (col >> 8 & 255) / 255.0f;
            float b = (col & 255) / 255.0f;
            ClientProxy.font.drawString(xoffset, 128 - currenty, text, 0.25f, 0.25f, -512f-40f, r, g, b, 1.0f);
        } else {
            fontRenderer.drawString(text, xoffset, currenty, col);
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
                labelCache.setAlign("Left".equals(alignment) ? 0 : ("Right".equals(alignment) ? 2 : 1));
            } else {
                labelCache.setAlign(0);
            }
            labelCache.setDirty();
        }
    }

    @Override
    public boolean needsServerData() {
        return true;
    }
}
