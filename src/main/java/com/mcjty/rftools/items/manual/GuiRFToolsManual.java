package com.mcjty.rftools.items.manual;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

public class GuiRFToolsManual extends GuiScreen {
    static final ResourceLocation texture = new ResourceLocation("minecraft:textures/gui/book.png");

    private int pageIndex = 0;

    public GuiRFToolsManual() {
    }

    @Override
    public void initGui() {
        super.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float renderPartials) {
        int bookXStart = (width - 192) / 2;
        mc.renderEngine.bindTexture(texture);
        drawTexturedModalRect(bookXStart, 2, 0, 0, 192, 192);

//        fontRendererObj.drawString("§n" + page.getDisplayName(), bookXStart + 40 + 4 + 16, 17, 0x000000);
//        fontRendererObj.drawSplitString(page.getDescriptionText(), bookXStart + 40, 17 + 15, 115, 0x000000);

        super.drawScreen(mouseX, mouseY, renderPartials);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    protected void keyTyped(char c, int key) {
        char lowerCase = Character.toLowerCase(c);
        if (key == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(null);
        }
    }
}
