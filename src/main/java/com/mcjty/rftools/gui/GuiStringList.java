package com.mcjty.rftools.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.renderer.Tessellator;

public class GuiStringList extends GuiListExtended {
    private Minecraft mc;

    public GuiStringList(Minecraft mc, int width, int height, int top, int bottom, int slotHeight) {
        super(mc, width, height, top, bottom, slotHeight);
        this.mc = mc;
    }

    @Override
    public IGuiListEntry getListEntry(int i) {
        switch (i) {
            case 0: return new StringEntry(this, "First item");
            case 1: return new StringEntry(this, "Second item");
            case 2: return new StringEntry(this, "Third item");
        }
        return null;
    }

    @Override
    protected int getSize() {
        return 3;
    }

    public static class StringEntry implements IGuiListEntry {
        private final GuiStringList owningList;
        private final String value;

        public StringEntry(GuiStringList owningList, String value) {
            this.owningList = owningList;
            this.value = value;
        }

        @Override
        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, Tessellator tessellator, int mouseX, int mouseY, boolean isSelected) {
            owningList.mc.fontRenderer.drawString(value, x + 1, y, 1677215); //slotIndex == owningList.selectedIndex ? 16777215 : 14737632);
        }

        @Override
        public boolean mousePressed(int index, int x, int y, int mouseEvent, int relativeX, int relativeY) {
            return false;
        }

        @Override
        public void mouseReleased(int index, int x, int y, int mouseEvent, int relativeX, int relativeY) {

        }
    }
}
