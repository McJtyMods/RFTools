package com.mcjty.gui.widgets;

import com.mcjty.gui.Window;
import com.mcjty.gui.events.TextEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

public class TextField extends AbstractWidget<TextField> {
    private String text = "";
    private int cursor = 0;
    private int startOffset = 0;        // Start character where we are displaying
    private List<TextEvent> textEvents = null;

    public TextField(Minecraft mc, Gui gui) {
        super(mc, gui);
    }

    public String getText() {
        return text;
    }

    public TextField setText(String text) {
        this.text = text;
        cursor = text.length();
        if (startOffset >= cursor) {
            startOffset = cursor-1;
            if (startOffset < 0) {
                startOffset = 0;
            }
        }
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
    public boolean keyTyped(Window window, char typedChar, int keyCode) {
        boolean rc = super.keyTyped(window, typedChar, keyCode);
        if (rc) {
            return true;
        }
        if (enabled) {
            if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_ESCAPE) {
                window.setTextFocus(null);
            } else if (keyCode == Keyboard.KEY_BACK) {
                if (!text.isEmpty() && cursor > 0) {
                    text = text.substring(0, cursor-1) + text.substring(cursor);
                    cursor--;
                    fireTextEvents(text);
                }
            } else if (keyCode == Keyboard.KEY_DELETE) {
                if (cursor < text.length()) {
                    text = text.substring(0, cursor) + text.substring(cursor+1);
                    fireTextEvents(text);
                }
            } else if (keyCode == Keyboard.KEY_HOME) {
                cursor = 0;
            } else if (keyCode == Keyboard.KEY_END) {
                cursor = text.length();
            } else if (keyCode == Keyboard.KEY_LEFT) {
                if (cursor > 0) {
                    cursor--;
                }
            } else if (keyCode == Keyboard.KEY_RIGHT) {
                if (cursor < text.length()) {
                    cursor++;
                }
            } else if (new Integer(typedChar).intValue() == 0) {
                // Do nothing
            } else {
                text = text.substring(0, cursor) + typedChar + text.substring(cursor);
                cursor++;
                fireTextEvents(text);
            }
            return true;
        }
        return false;
    }

    private int calculateVerticalOffset() {
        int h = mc.fontRenderer.FONT_HEIGHT;
        return (bounds.height - h)/2;
    }

    private void ensureVisible() {
        if (cursor < startOffset) {
            startOffset = cursor;
        } else {
            int w = mc.fontRenderer.getStringWidth(text.substring(startOffset, cursor));
            while (w > bounds.width-12) {
                startOffset++;
                w = mc.fontRenderer.getStringWidth(text.substring(startOffset, cursor));
            }
        }
    }


    @Override
    public void draw(Window window, int x, int y) {
        super.draw(window, x, y);

        int xx = x + bounds.x;
        int yy = y + bounds.y;

        ensureVisible();

        int col = 0xff000000;
        if (window.getTextFocus() == this) {
            col = 0xff444444;
        }
        Gui.drawRect(xx, yy, xx + bounds.width - 1, yy + bounds.height - 1, col);

        mc.fontRenderer.drawString(mc.fontRenderer.trimStringToWidth(text.substring(startOffset), bounds.width-10), x + 5 + bounds.x, y + calculateVerticalOffset() + bounds.y, 0xffffffff);

        if (window.getTextFocus() == this) {
            int w = mc.fontRenderer.getStringWidth(text.substring(startOffset, cursor));
            Gui.drawRect(xx+5+w, yy, xx+5+w+2, yy + bounds.height - 1, 0xffffffff);
        }
    }

    public TextField addTextEvent(TextEvent event) {
        if (textEvents == null) {
            textEvents = new ArrayList<TextEvent>();
        }
        textEvents.add(event);
        return this;
    }

    public void removeTextEvent(TextEvent event) {
        if (textEvents != null) {
            textEvents.remove(event);
        }
    }

    private void fireTextEvents(String newText) {
        if (textEvents != null) {
            for (TextEvent event : textEvents) {
                event.textChanged(this, newText);
            }
        }
    }
}
