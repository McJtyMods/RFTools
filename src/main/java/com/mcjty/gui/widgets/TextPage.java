package com.mcjty.gui.widgets;

import com.mcjty.gui.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

import java.util.ArrayList;
import java.util.List;

public class TextPage extends AbstractWidget<TextPage> {
    private final List<String> lines = new ArrayList<String>();

    public TextPage(Minecraft mc, Gui gui) {
        super(mc, gui);
    }

    public TextPage addLine(String line) {
        lines.add(line);
        return this;
    }

    public TextPage setPage(Page page) {
        lines.clear();
        for (String line : page.lines) {
            addLine(line);
        }
        return this;
    }


    @Override
    public void draw(Window window, int x, int y) {
        super.draw(window, x, y);

        y += 3;
        for (String line : lines) {
            mc.fontRenderer.drawString(mc.fontRenderer.trimStringToWidth(line, bounds.width), x + bounds.x, y + bounds.y, 0xFF000000);
            y += 14;
        }
    }

    public static class Page {
        final List<String> lines = new ArrayList<String>();

        public boolean isEmpty() {
            return lines.isEmpty();
        }

        public void addLine(String line) {
            lines.add(line);
        }
    }


}
