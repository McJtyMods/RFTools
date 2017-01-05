package mcjty.rftools.blocks.screens.modulesclient;

import mcjty.lib.gui.RenderHelper;
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

public class ButtonClientScreenModule implements IClientScreenModule<IModuleDataBoolean> {
    private String line = "";
    private String button = "";
    private boolean toggle = false;
    private int color = 0xffffff;
    private int buttonColor = 0xffffff;
    private boolean activated = false;
    private int align = 0;  // 0 == left, 1 == center, 2 == right

    private boolean dirty = true;
    private int labelx;
    private String labelLine;
    private String buttonLine;

    @Override
    public TransformMode getTransformMode() {
        return TransformMode.TEXT;
    }

    @Override
    public int getHeight() {
        return 14;
    }

    private void setup(FontRenderer fontRenderer) {
        if (!dirty) {
            return;
        }
        dirty = false;

        int xoffset;
        if (!line.isEmpty()) {
            int w = 74;
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
            xoffset = 7 + 80;
        } else {
            xoffset = 7 + 5;
        }
        buttonLine = fontRenderer.trimStringToWidth(button, 130 - 7 - xoffset);
    }


    @Override
    public void render(IModuleRenderHelper renderHelper, FontRenderer fontRenderer, int currenty, IModuleDataBoolean screenData, ModuleRenderInfo renderInfo) {
        GlStateManager.disableLighting();
        GlStateManager.enableDepth();
        GlStateManager.depthMask(false);
        setup(fontRenderer);

        int xoffset;
        if (!line.isEmpty()) {
            fontRenderer.drawString(labelLine, labelx, currenty + 2, color);
            xoffset = 7 + 80;
        } else {
            xoffset = 7 + 5;
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
        fontRenderer.drawString(buttonLine, xoffset + (act ? 1 : 0), currenty + 2 + (act ? 1 : 0), buttonColor);
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
    public void createGui(IModuleGuiBuilder guiBuilder) {
        guiBuilder
                .label("Label:").text("text", "Label text").color("color", "Label color").nl()
                .label("Button:").text("button", "Button text").color("buttonColor", "Button color").nl()
                .toggle("toggle", "Toggle", "Toggle button mode")
                .choices("align", "Label alignment", "Left", "Center", "Right").nl();

    }

    @Override
    public void setupFromNBT(NBTTagCompound tagCompound, int dim, BlockPos pos) {
        if (tagCompound != null) {
            line = tagCompound.getString("text");
            button = tagCompound.getString("button");
            if (tagCompound.hasKey("color")) {
                color = tagCompound.getInteger("color");
            } else {
                color = 0xffffff;
            }
            if (tagCompound.hasKey("buttonColor")) {
                buttonColor = tagCompound.getInteger("buttonColor");
            } else {
                buttonColor = 0xffffff;
            }
            toggle = tagCompound.getBoolean("toggle");
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
