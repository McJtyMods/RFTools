package com.mcjty.gui.widgets;

import com.mcjty.gui.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.lwjgl.input.Keyboard;

public class TextField extends AbstractWidget<TextField> {
    private String text = "";
    private int cursor = 0;

    public TextField(Minecraft mc, Gui gui) {
        super(mc, gui);
    }

    public String getText() {
        return text;
    }

    public TextField setText(String text) {
        this.text = text;
        cursor = text.length();
        return this;
    }

    @Override
    public Widget mouseClick(Window window, int x, int y, int button) {
        if (enabled) {
            window.setTextFocus(this);
            return this;
        }
        return null;
    }

    @Override
    public void keyTyped(Window window, char typedChar, int keyCode) {
        super.keyTyped(window, typedChar, keyCode);
        if (enabled) {
            System.out.print("typedChar = " + typedChar);
            System.out.println(", keyCode = " + keyCode);
            if (keyCode == Keyboard.KEY_RETURN) {
                window.setTextFocus(null);
            } else if (keyCode == Keyboard.KEY_BACK) {
                if (!text.isEmpty() && cursor > 0) {
                    text = text.substring(0, cursor-1) + text.substring(cursor);
                    cursor--;
                }
            } else if (keyCode == Keyboard.KEY_LEFT) {
                if (cursor > 0) {
                    cursor--;
                }
            } else if (keyCode == Keyboard.KEY_RIGHT) {
                if (cursor < text.length()-1) {
                    cursor++;
                }
            } else {
                text = text.substring(0, cursor) + typedChar + text.substring(cursor);
                cursor++;
            }
        }
    }

    private int calculateVerticalOffset() {
        int h = mc.fontRenderer.FONT_HEIGHT;
        return (bounds.height - h)/2;
    }


    @Override
    public void draw(Window window, int x, int y) {
        super.draw(window, x, y);

        int xx = x + bounds.x;
        int yy = y + bounds.y;

        int col = 0xff000000;
        if (window.getTextFocus() == this) {
            col = 0xff444444;
        }
        Gui.drawRect(xx, yy, xx + bounds.width - 1, yy + bounds.height - 1, col);

        mc.fontRenderer.drawString(mc.fontRenderer.trimStringToWidth(text, bounds.width-10), x + 5 + bounds.x, y + calculateVerticalOffset() + bounds.y, 0xffffffff);

        if (window.getTextFocus() == this) {
            int w = mc.fontRenderer.getStringWidth(text.substring(0, cursor));
            Gui.drawRect(xx+5+w, yy, xx+5+w+2, yy + bounds.height - 1, 0xffffffff);
        }
    }
}
